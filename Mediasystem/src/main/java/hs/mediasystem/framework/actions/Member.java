package hs.mediasystem.framework.actions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Member {
  private final Field field;
  private final Method method;

  public Member(Field field) {
    this.field = field;
    this.method = null;
  }

  public Member(Method method) {
    this.field = null;
    this.method = method;
  }

  public Method getMethod() {
    return method;
  }

  public Class<?> getType() {
    return field != null ? field.getType() : method.getReturnType();
  }

  public Class<?> getDeclaringClass() {
    return field != null ? field.getDeclaringClass() : method.getDeclaringClass();
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return field != null ? field.getAnnotation(annotationClass) : method.getAnnotation(annotationClass);
  }

  public Annotation[] getAnnotations() {
    return field != null ? field.getAnnotations() : method.getAnnotations();
  }

  public String getName() {
    return field != null ? field.getName() : method.getName();
  }

  public Object get(Object root) {
    try {
      return field != null ? field.get(root) : method.invoke(root);
    }
    catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException("Exception accessing: " + toString(), e);
    }
  }

  @Override
  public String toString() {
    return "Member[" + (field != null ? field : method) + "]";
  }
}
