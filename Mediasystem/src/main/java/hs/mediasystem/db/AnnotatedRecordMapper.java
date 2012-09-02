package hs.mediasystem.db;

import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.util.WeakValueMap;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class AnnotatedRecordMapper<T> implements RecordMapper<T> {
  private static final Map<Class<?>, AnnotatedRecordMapper<?>> RECORD_MAPPERS = new WeakValueMap<>();

  private final Map<Accessor, String[]> columns = new HashMap<>();
  private final Map<Class<?>, String[]> relations = new HashMap<>();
  private final String tableName;

  private Accessor idAccessor;
  private String[] idColumnNames;
  private MethodHandle afterLoadStore;

  private AnnotatedRecordMapper(Class<T> cls) {
    Table table = cls.getAnnotation(Table.class);

    tableName = table.name();

    List<Accessor> accessors = getAnnotatedAccessors(cls, null, Column.class, Id.class);

    for(Accessor accessor : accessors) {
      Id id = accessor.getAnnotation(Id.class);
      Column column = accessor.getAnnotation(Column.class);

      String[] columnNames = column == null ? null : column.name();

      if(columnNames == null || columnNames.length == 0) {
        columnNames = new String[] {accessor.getName()};
      }

      for(int i = 0; i < columnNames.length; i++) {
        columnNames[i] = columnNames[i].toLowerCase();
      }

      if(id != null && idAccessor != null) {
        throw new IllegalStateException("only one @Id annotation allowed: " + cls);
      }
      else if(id != null) {
        idAccessor = accessor;
        idColumnNames = columnNames;
      }
      else if(column != null) {
        columns.put(accessor, columnNames);
      }

      if(isRelation(accessor.getType())) {
        relations.put(accessor.getType(), columnNames);
      }
    }

    try {
      afterLoadStore = MethodHandles.lookup().findVirtual(cls, "afterLoadStore", MethodType.methodType(void.class, Database.class));
    }
    catch(NoSuchMethodException | IllegalAccessException e) {
      // ignore and continue
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
    return new ArrayList<>(Arrays.asList(idColumnNames));
  }

  public Object[] getIds(T instance) {
    Object value = idAccessor.get(instance);

    if(idAccessor.getType().getAnnotation(IdClass.class) == null) {
      return new Object[] {value};
    }

    List<Accessor> accessors = getAnnotatedAccessors(idAccessor.getType(), new Comparator<Accessor>() {
      @Override
      public int compare(Accessor o1, Accessor o2) {
        return Integer.compare(o1.getAnnotation(IdColumn.class).value(), o2.getAnnotation(IdColumn.class).value());
      }
    }, IdColumn.class);


    Object[] ids = new Object[accessors.size()];
    int index = 0;

    for(Accessor accessor : accessors) {
      ids[index++] = accessor.get(value);
    }

    return ids;
  }

  @Override
  public Map<String, Object> extractValues(T object) {
    Map<String, Object> values = new HashMap<>();

    for(Accessor accessor : columns.keySet()) {
      Object value = accessor.get(object);

      if(value != null && isRelation(value.getClass())) {
        @SuppressWarnings("unchecked")
        AnnotatedRecordMapper<Object> mapper = (AnnotatedRecordMapper<Object>)create(value.getClass());

        Object[] ids = mapper.getIds(value);
        String[] columnNames = columns.get(accessor);

        for(int i = 0; i < ids.length; i++) {
          values.put(columnNames[i], ids[i]);
        }
      }
      else {
        for(String columnName : columns.get(accessor)) {
          values.put(columnName, value);
        }
      }
    }

    return values;
  }

  @Override
  public Map<String, Object> extractIds(T object) {
    Map<String, Object> values = new HashMap<>();

    Object[] ids = getIds(object);

    for(int i = 0; i < ids.length; i++) {
      values.put(idColumnNames[i], ids[i]);
    }

    return values;
  }

  @Override
  public void applyValues(Transaction transaction, T object, Map<String, Object> map) {
    for(Accessor accessor : columns.keySet()) {
      accessor.set(object, convertValue(columns.get(accessor), accessor.getType(), map, transaction));
    }

    if(idAccessor != null) {
      idAccessor.set(object, convertValue(idColumnNames, idAccessor.getType(), map, transaction));
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

  private Object convertValue(String[] columnNames, Class<?> type, Map<String, Object> map, Transaction transaction) {
    try {
      if(type.getAnnotation(IdClass.class) != null) {
        Class<?>[] types = new Class<?>[columnNames.length];
        Object[] idValues = new Object[columnNames.length];

        for(int i = 0; i < columnNames.length; i++) {
          Object value = map.get(columnNames[i]);

          types[i] = value.getClass();
          idValues[i] = value;
        }

        Constructor<?> constructor = type.getConstructor(types);

        return constructor.newInstance(idValues);
      }
      else if(isRelation(type)) {
        Object[] idValues = new Object[columnNames.length];

        for(int i = 0; i < columnNames.length; i++) {
          idValues[i] = map.get(columnNames[i]);
        }

        Object associatedObject = transaction.findAssociatedObject(type, idValues);

        if(associatedObject == null) {
          associatedObject = type.newInstance();
          associateStub(transaction, associatedObject, idValues);
        }

        return associatedObject;
      }

      Object value = map.get(columnNames[0]);

      if(type.isEnum() && value instanceof String) {
        Method method = type.getMethod("valueOf", String.class);

        return method.invoke(null, value);
      }
      else if(type.isArray() && value instanceof String) {
        return ((String)value).split(",");
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
    if(idAccessor == null) {
      return;
    }

    if(key instanceof Number) {
      Number number = (Number)key;

      if(idAccessor.getType() == int.class || idAccessor.getType() == Integer.class) {
        idAccessor.set(object, number.intValue());
      }
      else {
        idAccessor.set(object, number.longValue());
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
    <A extends Annotation> A getAnnotation(Class<A> cls);
    String getName();
  }

  @SafeVarargs
  public static List<Accessor> getAnnotatedAccessors(Class<?> cls, Comparator<Accessor> comparator, Class<? extends Annotation>... annotationClasses) {
    List<Accessor> accessors = new ArrayList<>();
    List<AccessibleObject> accessibleObjects = new ArrayList<>();

    accessibleObjects.addAll(Arrays.asList(cls.getDeclaredFields()));
    accessibleObjects.addAll(Arrays.asList(cls.getDeclaredMethods()));

    for(AccessibleObject accessibleObject : accessibleObjects) {
      for(Class<? extends Annotation> annotationClass : annotationClasses) {
        if(accessibleObject.getAnnotation(annotationClass) != null) {
          accessors.add(createAccessor(accessibleObject));
          break;
        }
      }
    }

    if(comparator != null) {
      Collections.sort(accessors, comparator);
    }

    return accessors;
  }

  public static String getAccessibleObjectName(AccessibleObject accessibleObject) {
    if(accessibleObject instanceof Field) {
      return ((Field)accessibleObject).getName();
    }

    Method method = (Method)accessibleObject;

    return getBaseNameFromGetter(method);
  }

  protected static String getBaseNameFromGetter(Method method) {
    String methodName = method.getName();

    if(methodName.startsWith("get")) {
      methodName = methodName.substring(3);
    }
    else if(methodName.startsWith("is")) {
      methodName = methodName.substring(2);
    }

    return methodName;
  }

  public static Accessor createAccessor(AccessibleObject accessibleObject) {
    if(accessibleObject instanceof Field) {
      return new FieldAccessor((Field)accessibleObject);
    }

    Method method = (Method)accessibleObject;
    String baseName = getBaseNameFromGetter(method);

    try {
      Method writeMethod = method.getDeclaringClass().getDeclaredMethod("set" + baseName, method.getReturnType());

      return new MethodAccessor(method, writeMethod);
    }
    catch(NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
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

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> cls) {
      return field.getAnnotation(cls);
    }

    @Override
    public String getName() {
      return field.getName();
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

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> cls) {
      return readMethod.getAnnotation(cls);
    }

    @Override
    public String getName() {
      String name = readMethod.getName();

      return name.startsWith("get") ? name.substring(3) : name.substring(2);
    }
  }
}
