package hs.mediasystem.framework.actions;

import hs.mediasystem.config.Resources;
import hs.mediasystem.util.StringConverter;

import java.lang.annotation.RetentionPolicy;

import javafx.event.Event;

@java.lang.annotation.Documented
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface Expose {
  String values() default "";
  Class<? extends ValueBuilder<?>> valueBuilder() default NullValueBuilder.class;
  Class<? extends StringConverter<?>> stringConverter() default ResourceBasedStringConverter.class;

  public static class ResourceBasedStringConverter implements StringConverter<Object> {
    @Override
    public String toString(Object object) {
      return Resources.getResource(object.getClass().getName(), "label");
    }
  }

  public static class NullValueBuilder implements ValueBuilder<Void> {
    @Override
    public Void build(Event event, Void currentValue) {
      throw new UnsupportedOperationException();
    }
  }
}
