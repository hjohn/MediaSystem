package hs.mediasystem.beans;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanAccessor<T> implements Accessor<T> {
  private final Object instance;
  private final String propertyName;

  private Method readMethod;
  private Method writeMethod;

  public BeanAccessor(Object instance, String propertyName) {
    this.instance = instance;
    this.propertyName = propertyName;
  }

  private Method getReadMethod() {
    if(readMethod == null) {
      readMethod = BeanUtils.getReadMethod(instance.getClass(), propertyName);
    }

    return readMethod;
  }

  private Class<?> getType() {
    return getReadMethod().getReturnType();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T read() {
    try {
      return (T)getReadMethod().invoke(instance);
    }
    catch(IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch(InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void write(T value) {
    if(writeMethod == null) {
      writeMethod = BeanUtils.getWriteMethod(instance.getClass(), propertyName, getType());
    }

    try {
      writeMethod.invoke(instance, value);
    }
    catch(IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch(InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
