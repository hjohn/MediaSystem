package hs.mediasystem.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;

public class MapBindings {
  public static StringBinding selectString(final ObservableValue<?> root, final Object... steps) {
    return new StringBinding() {
      private final Helper helper;

      {
        helper = new Helper(this, root, steps);
      }

      @Override
      public void dispose() {
        helper.unregisterListeners();
      }

      @Override
      protected void onInvalidating() {
        helper.unregisterListeners();
      }

      @Override
      protected String computeValue() {
        Object obj = helper.computeValue();

        return obj instanceof String ? (String)obj : null;
      }
    };
  }

  public static IntegerBinding selectInteger(final ObservableValue<?> root, final Object... steps) {
    return new IntegerBinding() {
      private final Helper helper;

      {
        helper = new Helper(this, root, steps);
      }

      @Override
      public void dispose() {
        helper.unregisterListeners();
      }

      @Override
      protected void onInvalidating() {
        helper.unregisterListeners();
      }

      @Override
      protected int computeValue() {
        Object obj = helper.computeValue();

        return obj instanceof Number ? ((Number)obj).intValue() : 0;
      }
    };
  }

  public static DoubleBinding selectDouble(final ObservableValue<?> root, final Object... steps) {
    return new DoubleBinding() {
      private final Helper helper;

      {
        helper = new Helper(this, root, steps);
      }

      @Override
      public void dispose() {
        helper.unregisterListeners();
      }

      @Override
      protected void onInvalidating() {
        helper.unregisterListeners();
      }

      @Override
      protected double computeValue() {
        Object obj = helper.computeValue();

        return obj instanceof Number ? ((Number)obj).doubleValue() : 0;
      }
    };
  }

  public static <T> ObjectBinding<T> select(final ObservableValue<?> root, final Object... steps) {
    return new ObjectBinding<T>() {
      private final Helper helper;

      {
        helper = new Helper(this, root, steps);
      }

      @Override
      public void dispose() {
        helper.unregisterListeners();
      }

      @Override
      protected void onInvalidating() {
        helper.unregisterListeners();
      }

      @SuppressWarnings("unchecked")
      @Override
      protected T computeValue() {
        return (T)helper.computeValue();
      }
    };
  }

  private static class Helper {
    private final ObservableValue<?>[] observableValues;
    private final Property[] properties;
    private final InvalidationListener listener = new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        binding.invalidate();
      }
    };
    private final WeakInvalidationListener observer = new WeakInvalidationListener(listener);
    private final ObservableValue<?> root;
    private final Object[] steps;
    private final Binding<?> binding;

    public Helper(Binding<?> binding, final ObservableValue<?> root, final Object... steps) {
      this.binding = binding;
      this.root = root;
      this.steps = steps;
      observableValues = new ObservableValue<?>[steps.length + 1];
      properties = new Property[steps.length];
    }

    private void unregisterListeners() {
      for(int index = 0; index < observableValues.length; index++) {
        if(observableValues[index] == null) {
          break;
        }
        observableValues[index].removeListener(this.observer);
        observableValues[index] = null;
      }
    }

    protected Object computeValue() {
      ObservableValue<?> observableValue = root;

      for(int index = 0; index <= steps.length; index++) {
        observableValues[index] = observableValue;
        observableValue.addListener(observer);

        if(index == steps.length) {
          break;
        }

        Object value = observableValue.getValue();

        try {
          if(properties[index] == null || !value.getClass().equals(properties[index].getBeanClass())) {
            properties[index] = new Property(value.getClass(), steps[index]);
          }

          observableValue = properties[index].getObservableValue(value);
        }
        catch(RuntimeException e) {
          return null;
        }
      }

      return observableValue.getValue();
    }
  }

  private static class Property {
    private final Class<?> cls;
    private final Object name;

    private Method method;

    public Property(Class<?> cls, Object name) {
      this.cls = cls;
      this.name = name;
    }

    @SuppressWarnings("unchecked")
    public ObservableValue<?> getObservableValue(Object bean) {
      try {
        if(name instanceof String) {
          if(method == null) {
            method = cls.getMethod(name + "Property");
          }

          return (ObservableValue<?>)method.invoke(bean);
        }

        return Bindings.valueAt((ObservableMap<Object, ?>)bean, name);
      }
      catch(NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        e.printStackTrace();
        return null;
      }
    }

    public Class<?> getBeanClass() {
      return cls;
    }
  }
}
