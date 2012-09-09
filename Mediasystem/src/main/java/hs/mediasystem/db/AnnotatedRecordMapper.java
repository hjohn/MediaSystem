package hs.mediasystem.db;

import hs.mediasystem.db.Column.DefaultConverter;
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
  private static final Comparator<Accessor> ACCESSOR_COMPARATOR = new Comparator<Accessor>() {
    @Override
    public int compare(Accessor o1, Accessor o2) {
      return Integer.compare(o1.getAnnotation(IdColumn.class).value(), o2.getAnnotation(IdColumn.class).value());
    }
  };

  private final List<Column> columns = new ArrayList<>();
  private final Map<Class<?>, String[]> relations = new HashMap<>();
  private final String tableName;

  private Column idColumn;
  private MethodHandle afterLoadStore;

  private AnnotatedRecordMapper(Class<T> cls) {
    Table table = cls.getAnnotation(Table.class);

    tableName = table.name();

    List<Accessor> accessors = getAnnotatedAccessors(cls, null, hs.mediasystem.db.Column.class, Id.class);

    for(Accessor accessor : accessors) {
      Id id = accessor.getAnnotation(Id.class);
      hs.mediasystem.db.Column column = accessor.getAnnotation(hs.mediasystem.db.Column.class);

      @SuppressWarnings("unchecked")
      Class<DataTypeConverter<Object, Object>> dataTypeConverterClass = (Class<DataTypeConverter<Object, Object>>)(column == null ? DefaultConverter.class : column.converterClass());
      String[] columnNames = column == null ? null : column.name();

      if(columnNames == null || columnNames.length == 0) {
        columnNames = new String[] {accessor.getName()};
      }

      for(int i = 0; i < columnNames.length; i++) {
        columnNames[i] = columnNames[i].toLowerCase();
      }

      if(id != null && idColumn != null) {
        throw new IllegalStateException("only one @Id annotation allowed: " + cls);
      }
      else if(id != null) {
        idColumn = new Column(columnNames, accessor, dataTypeConverterClass);
      }
      else if(column != null) {
        columns.add(new Column(columnNames, accessor, dataTypeConverterClass));
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

  private static boolean isRelation(Class<?> type) {
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

    for(Column column : columns) {
      columnNames.addAll(Arrays.asList(column.getNames()));
    }

    return columnNames;
  }

  public List<String> getIdColumnNames() {
    return new ArrayList<>(Arrays.asList(idColumn.getNames()));
  }

  public Object[] getIds(T instance) {
    Object value = idColumn.getAccessor().get(instance);

    if(!idColumn.isEmbedded()) {
      return new Object[] {value};
    }

    return getEmbeddedFields(value);
  }

  public Object[] getEmbeddedFields(Object embeddedInstance) {
    List<Accessor> accessors = getAnnotatedAccessors(embeddedInstance.getClass(), ACCESSOR_COMPARATOR, IdColumn.class);

    Object[] values = new Object[accessors.size()];
    int index = 0;

    for(Accessor accessor : accessors) {
      values[index++] = accessor.get(embeddedInstance);
    }

    return values;
  }

  @Override
  public Map<String, Object> extractValues(T object) {
    Map<String, Object> values = new HashMap<>();

    for(Column column : columns) {
      column.toStorageType(column.getAccessor().get(object), values);
    }

    return values;
  }

  @Override
  public Map<String, Object> extractIds(T object) {
    Map<String, Object> values = new HashMap<>();

    Object[] ids = getIds(object);

    for(int i = 0; i < ids.length; i++) {
      values.put(idColumn.getNames()[i], ids[i]);
    }

    return values;
  }

  @Override
  public boolean isTransient(T object) {
    for(Object value : extractIds(object).values()) {
      if(value != null) {
        return false;
      }
    }

    return true;
  }

  @Override
  public void applyValues(Transaction transaction, T object, Map<String, Object> map) {
    for(Column column : columns) {
      column.getAccessor().set(object, column.toJavaType(map, transaction));
    }

    if(idColumn != null) {
      idColumn.getAccessor().set(object, idColumn.toJavaType(map, transaction));
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

  @Override
  public void setGeneratedKey(T object, Object key) {
    if(idColumn == null) {
      return;
    }

    if(key instanceof Number) {
      Number number = (Number)key;

      if(idColumn.getType() == int.class || idColumn.getType() == Integer.class) {
        idColumn.getAccessor().set(object, number.intValue());
      }
      else {
        idColumn.getAccessor().set(object, number.longValue());
      }
    }
  }

  private static final Map<Object, Database> DATABASES = new HashMap<>();
  private static final Map<Object, Object[]> STUBS = new WeakHashMap<>();

  public void associateStub(Transaction transaction, Object stub, Object[] ids) {
    synchronized(DATABASES) {
      STUBS.put(stub, ids);
      DATABASES.put(stub, transaction.getDatabase());
    }
  }

  public static <T> T fetch(T instance) {
    Database database;
    Object[] ids;

    synchronized(DATABASES) {
      database = DATABASES.get(instance);
      ids = STUBS.get(instance);
    }

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

  public class Column {
    private final String[] names;
    private final Accessor accessor;
    private final DataTypeConverter<Object, Object> dataTypeConverter;

    public Column(String[] names, Accessor accessor, Class<DataTypeConverter<Object, Object>> dataTypeConverterClass) {
      this.names = names;
      this.accessor = accessor;

      try {
        this.dataTypeConverter = dataTypeConverterClass.newInstance();
      }
      catch(IllegalAccessException | InstantiationException e) {
        throw new RuntimeException("Exception while creating DataTypeConverter: " + dataTypeConverterClass, e);
      }
    }

    public String[] getNames() {
      return names;
    }

    public Accessor getAccessor() {
      return accessor;
    }

    public Class<?> getType() {
      return accessor.getType();
    }

    public boolean isEmbedded() {
      return getType().getAnnotation(IdClass.class) != null;
    }

    public void toStorageType(Object value, Map<String, Object> valuesRef) {
      Object[] values = null;

      if(isRelation(getType())) {
        if(value != null) {
          @SuppressWarnings("unchecked")
          AnnotatedRecordMapper<Object> mapper = (AnnotatedRecordMapper<Object>)create(getType());

          values = mapper.getIds(value);
        }
      }
      else if(isEmbedded()) {
        if(value != null) {
          values = getEmbeddedFields(value);
        }
      }
      else {
        values = new Object[] {dataTypeConverter.toStorageType(value)};
      }

      if(values != null) {
        for(int i = 0; i < names.length; i++) {
          valuesRef.put(names[i], values[i]);
        }
      }
      else {
        for(String columnName : names) {
          valuesRef.put(columnName, null);
        }
      }
    }

    public Object toJavaType(Map<String, Object> map, Transaction transaction) {
      try {
        if(isEmbedded()) {
          Class<?>[] types = new Class<?>[names.length];
          Object[] idValues = new Object[names.length];

          for(int i = 0; i < names.length; i++) {
            Object value = map.get(names[i]);

            if(value == null) {
              return null;
            }

            types[i] = value.getClass();
            idValues[i] = value;
          }

          Constructor<?> constructor = getType().getConstructor(types);

          return constructor.newInstance(idValues);
        }
        else if(isRelation(getType())) {
          Object[] idValues = new Object[names.length];

          for(int i = 0; i < names.length; i++) {
            idValues[i] = map.get(names[i]);
          }

          Object associatedObject = transaction.findAssociatedObject(getType(), idValues);

          if(associatedObject == null) {
            associatedObject = getType().newInstance();
            associateStub(transaction, associatedObject, idValues);
          }

          return associatedObject;
        }

        return dataTypeConverter.toJavaType(map.get(names[0]), getType());
      }
      catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
