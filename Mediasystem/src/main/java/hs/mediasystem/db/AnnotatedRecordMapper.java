package hs.mediasystem.db;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedRecordMapper<T> implements RecordMapper<T> {
  private final Map<Accessor, String> ids = new HashMap<>();
  private final Map<Accessor, String> columns = new HashMap<>();
  private final String tableName;

  private MethodHandle afterLoadStore;

  public AnnotatedRecordMapper(Class<T> cls) {
    Table table = cls.getAnnotation(Table.class);

    tableName = table.name();

    for(Field field : cls.getDeclaredFields()) {
      Id id = field.getAnnotation(Id.class);
      Column column = field.getAnnotation(Column.class);

      if(id != null || column != null) {
        String columnName = column == null ? "" : column.name();

        if(columnName.isEmpty()) {
          columnName = field.getName().toLowerCase();
        }

        if(id != null) {
          ids.put(new FieldAccessor(field), columnName);
        }
        else if(column != null) {
          columns.put(new FieldAccessor(field), columnName);
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
          String columnName = column == null ? "" : column.name();

          String methodName = method.getName();

          if(methodName.startsWith("get")) {
            methodName = methodName.substring(3);
          }
          else if(methodName.startsWith("is")) {
            methodName = methodName.substring(2);
          }

          if(columnName.isEmpty()) {
            columnName = methodName.toLowerCase();
          }

          Method writeMethod = cls.getDeclaredMethod("set" + methodName, method.getReturnType());

          if(id != null) {
            ids.put(new MethodAccessor(method, writeMethod), columnName);
          }
          else if(column != null) {
            columns.put(new MethodAccessor(method, writeMethod), columnName);
          }
        }
      }
    }
    catch(NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getTableName() {
    return tableName;
  }

  @Override
  public Map<String, Object> extractValues(T object) {
    Map<String, Object> values = new HashMap<>();

    for(Accessor accessor : columns.keySet()) {
      values.put(columns.get(accessor), accessor.get(object));
    }

    return values;
  }

  @Override
  public Map<String, Object> extractIds(T object) {
    Map<String, Object> values = new HashMap<>();

    for(Accessor accessor : ids.keySet()) {
      Object id = accessor.get(object);

      if(id != null) {
        values.put(ids.get(accessor), id);
      }
    }

    return values;
  }

  @Override
  public void applyValues(T object, Map<String, Object> map) throws SQLException {
    for(Accessor accessor : columns.keySet()) {
      String fieldName = columns.get(accessor);

      if(map.containsKey(fieldName)) {
        accessor.set(object, convertValue(accessor, map.get(fieldName)));
      }
    }

    for(Accessor accessor : ids.keySet()) {
      String fieldName = ids.get(accessor);

      if(map.containsKey(fieldName)) {
        accessor.set(object, convertValue(accessor, map.get(fieldName)));
      }
    }
  }

  @Override
  public void invokeAfterLoadStore(T object, Database database) throws SQLException {
    if(afterLoadStore != null) {
      try {
        afterLoadStore.invoke(object, database);
      }
      catch(SQLException e) {
        throw e;
      }
      catch(Throwable e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Object convertValue(Accessor accessor, Object value) {
    try {
      if(accessor.getType().isEnum()) {
        if(value instanceof String) {
          Method method = accessor.getType().getMethod("valueOf", String.class);

          return method.invoke(null, value);
        }
      }

      return value;
    }
    catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setGeneratedKeys(T object, Map<String, Object> keys) {
    for(Accessor accessor : ids.keySet()) {
      String fieldName = ids.get(accessor);

      if(keys.containsKey(fieldName)) {
        accessor.set(object, keys.get(fieldName));
      }
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
