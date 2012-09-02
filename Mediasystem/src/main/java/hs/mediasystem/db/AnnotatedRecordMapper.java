package hs.mediasystem.db;

import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.util.WeakValueMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class AnnotatedRecordMapper<T> implements RecordMapper<T> {
  private static final Map<Class<?>, AnnotatedRecordMapper<?>> RECORD_MAPPERS = new WeakValueMap<>();

  private final Map<Accessor, String[]> ids = new HashMap<>();
  private final Map<Accessor, String[]> columns = new HashMap<>();
  private final Map<Class<?>, String[]> relations = new HashMap<>();
  private final String tableName;

  private MethodHandle afterLoadStore;

  private AnnotatedRecordMapper(Class<T> cls) {
    Table table = cls.getAnnotation(Table.class);

    tableName = table.name();

    for(Field field : cls.getDeclaredFields()) {
      Id id = field.getAnnotation(Id.class);
      Column column = field.getAnnotation(Column.class);

      if(id != null || column != null) {
        String[] columnNames = column == null ? null : column.name();

        if(columnNames == null || columnNames.length == 0) {
          columnNames = new String[] {field.getName().toLowerCase()};
        }

        if(id != null) {
          ids.put(new FieldAccessor(field), columnNames);
        }
        else if(column != null) {
          columns.put(new FieldAccessor(field), columnNames);
        }

        if(isRelation(field.getType())) {
          relations.put(field.getType(), columnNames);
        }
      }
    }

    try {
      afterLoadStore = MethodHandles.lookup().findVirtual(cls, "afterLoadStore", MethodType.methodType(void.class, Database.class));
    }
    catch(NoSuchMethodException | IllegalAccessException e) {
      // ignore and continue
    }

    try {
      for(Method method : cls.getDeclaredMethods()) {
        Id id = method.getAnnotation(Id.class);
        Column column = method.getAnnotation(Column.class);

        if(id != null || column != null) {
          String[] columnNames = column == null ? new String[0] : column.name();

          String methodName = method.getName();

          if(methodName.startsWith("get")) {
            methodName = methodName.substring(3);
          }
          else if(methodName.startsWith("is")) {
            methodName = methodName.substring(2);
          }

          if(columnNames == null || columnNames.length == 0) {
            columnNames = new String[] {methodName.toLowerCase()};
          }

          Method writeMethod = cls.getDeclaredMethod("set" + methodName, method.getReturnType());

          if(id != null) {
            ids.put(new MethodAccessor(method, writeMethod), columnNames);
          }
          else if(column != null) {
            columns.put(new MethodAccessor(method, writeMethod), columnNames);
          }

          if(isRelation(method.getReturnType())) {
            relations.put(method.getReturnType(), columnNames);
          }
        }
      }
    }
    catch(NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> AnnotatedRecordMapper<T> create(Class<T> cls) {
    @SuppressWarnings("unchecked")
    AnnotatedRecordMapper<T> annotatedRecordMapper = (AnnotatedRecordMapper<T>)RECORD_MAPPERS.get(cls);

    if(annotatedRecordMapper == null) {
      annotatedRecordMapper = new AnnotatedRecordMapper<>(cls);
      RECORD_MAPPERS.put(cls, annotatedRecordMapper);
    }

    return annotatedRecordMapper;
  }

  private boolean isRelation(Class<?> type) {
    return type.getAnnotation(Table.class) != null;
  }

  @Override
  public String getTableName() {
    return tableName;
  }

  public List<String> getRelationColumnNames(Class<?> relation) {
    List<String> relationColumnNames = new ArrayList<>();

    relationColumnNames.addAll(Arrays.asList(relations.get(relation)));

    return relationColumnNames;
  }

  public List<String> getColumnNames() {
    List<String> columnNames = new ArrayList<>();

    for(String[] names : columns.values()) {
      columnNames.addAll(Arrays.asList(names));
    }

    return columnNames;
  }

  public List<String> getIdColumnNames() {
    List<String> idColumnNames = new ArrayList<>();

    for(String[] names : ids.values()) {
      idColumnNames.addAll(Arrays.asList(names));
    }

    return idColumnNames;
  }

  @Override
  public Map<String, Object> extractValues(T object) {
    Map<String, Object> values = new HashMap<>();

    for(Accessor accessor : columns.keySet()) {
      values.put(columns.get(accessor)[0], accessor.get(object));
    }

    return values;
  }

  @Override
  public Map<String, Object> extractIds(T object) {
    Map<String, Object> values = new HashMap<>();

    for(Accessor accessor : ids.keySet()) {
      Object id = accessor.get(object);

      if(id != null) {
        values.put(ids.get(accessor)[0], id);
      }
    }

    return values;
  }

  @Override
  public void applyValues(Transaction transaction, T object, Map<String, Object> map) {
    for(Accessor accessor : columns.keySet()) {
      accessor.set(object, convertValue(accessor, map, transaction));
    }

    for(Accessor accessor : ids.keySet()) {
      accessor.set(object, convertValue(accessor, map, transaction));
    }
  }

  @Override
  public void invokeAfterLoadStore(T object, Database database) throws DatabaseException {
    if(afterLoadStore != null) {
      try {
        afterLoadStore.invoke(object, database);
      }
      catch(DatabaseException e) {
        throw e;
      }
      catch(Throwable e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Object convertValue(Accessor accessor, Map<String, Object> map, Transaction transaction) {
    String[] columnNames = columns.containsKey(accessor) ? columns.get(accessor) : ids.get(accessor);

    try {
      if(accessor.getType().getAnnotation(IdClass.class) != null) {
        Class<?>[] types = new Class<?>[columnNames.length];
        Object[] idValues = new Object[columnNames.length];

        for(int i = 0; i < columnNames.length; i++) {
          Object value = map.get(columnNames[i]);

          types[i] = value.getClass();
          idValues[i] = value;
        }

        Constructor<?> constructor = accessor.getType().getConstructor(types);

        return constructor.newInstance(idValues);
      }
      else if(isRelation(accessor.getType())) {
        Object[] idValues = new Object[columnNames.length];

        for(int i = 0; i < columnNames.length; i++) {
          idValues[i] = map.get(columnNames[i]);
        }

        Object associatedObject = transaction.findAssociatedObject(accessor.getType(), idValues);

        if(associatedObject == null) {
          associatedObject = accessor.getType().newInstance();
          associateStub(transaction, associatedObject, idValues);
        }

        return associatedObject;
      }

      Object value = map.get(columnNames[0]);

      if(accessor.getType().isEnum()) {
        if(value instanceof String) {
          Method method = accessor.getType().getMethod("valueOf", String.class);

          return method.invoke(null, value);
        }
      }
      else if(value instanceof Blob) {
        Blob blob = (Blob)value;

        return blob.getBytes(1L, (int)blob.length());
      }

      return value;
    }
    catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SQLException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setGeneratedKey(T object, Object key) {
    if(ids.isEmpty()) {
      return;
    }

    if(ids.size() != 1) {
      throw new IllegalStateException("cannot set generated keys for table with more than one id field: " + tableName);
    }

    Accessor accessor = ids.keySet().iterator().next();

    if(key instanceof Number) {
      Number number = (Number)key;

      if(accessor.getType() == int.class || accessor.getType() == Integer.class) {
        accessor.set(object, number.intValue());
      }
      else {
        accessor.set(object, number.longValue());
      }
    }
  }

  private static final Map<Object, Database> DATABASES = new HashMap<>();
  private static final Map<Object, Object[]> STUBS = new WeakHashMap<>();

  public void associateStub(Transaction transaction, Object stub, Object[] ids) {
    STUBS.put(stub, ids);
    DATABASES.put(stub, transaction.getDatabase());
  }

  public static <T> T fetch(T instance) {
    Database database = DATABASES.get(instance);
    Object[] ids = STUBS.get(instance);

    if(database == null || ids == null) {
      return instance;
    }

    AnnotatedRecordMapper<? extends Object> mapper = create(instance.getClass());

    String whereClause = "";

    for(String fieldName : mapper.getIdColumnNames()) {
      if(!whereClause.isEmpty()) {
        whereClause += " AND ";
      }
      whereClause += fieldName + "=?";
    }

    try(Transaction transaction = database.beginTransaction()) {
      @SuppressWarnings("unchecked")
      T result = (T)transaction.selectUnique(instance.getClass(), whereClause, ids);
      return result;
    }
  }

  public static <C> List<C> fetch(Class<C> cls, Object filteredBy) {
    Database database = Database.getAssociatedDatabase(cls);

    @SuppressWarnings("unchecked")
    AnnotatedRecordMapper<Object> parentMapper = (AnnotatedRecordMapper<Object>)create(filteredBy.getClass());
    AnnotatedRecordMapper<?> mapper = create(cls);

    List<String> relationColumnNames = mapper.getRelationColumnNames(filteredBy.getClass());
    List<String> idColumnNames = parentMapper.getIdColumnNames();
    Map<String, Object> values = parentMapper.extractIds(filteredBy);

    String relationExpression = "";
    Object[] parameters = new Object[relationColumnNames.size()];

    for(int i = 0; i < relationColumnNames.size(); i++) {
      relationExpression += relationColumnNames.get(i) + "=?";
      parameters[i] = values.get(idColumnNames.get(i));

      if(parameters[i] == null) {
        return new ArrayList<>();
      }
    }

    try(Transaction transaction = database.beginTransaction()) {
      transaction.associate(filteredBy);

      return transaction.select(cls, relationExpression, parameters);
    }
  }

  public interface Accessor {
    Object get(Object instance);
    void set(Object instance, Object value);
    Class<?> getType();
  }

  public static class FieldAccessor implements Accessor {
    private final Field field;

    public FieldAccessor(Field field) {
      this.field = field;

      field.setAccessible(true);
    }

    @Override
    public Object get(Object instance) {
      try {
        return field.get(instance);
      }
      catch(IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void set(Object instance, Object value) {
      try {
        field.set(instance, value);
      }
      catch(IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Class<?> getType() {
      return field.getType();
    }
  }

  public static class MethodAccessor implements Accessor {
    private final Method readMethod;
    private final Method writeMethod;

    public MethodAccessor(Method readMethod, Method writeMethod) {
      this.readMethod = readMethod;
      this.writeMethod = writeMethod;
    }

    @Override
    public Object get(Object instance) {
      try {
        return readMethod.invoke(instance);
      }
      catch(IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void set(Object instance, Object value) {
      try {
        writeMethod.invoke(instance, value);
      }
      catch(IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public Class<?> getType() {
      return readMethod.getReturnType();
    }
  }
}
