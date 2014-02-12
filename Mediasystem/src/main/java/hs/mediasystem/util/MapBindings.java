package hs.mediasystem.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class MapBindings {
  public static StringBinding selectString(final Observable root, final Object... steps) {
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

  public static IntegerBinding selectInteger(final Observable root, final Object... steps) {
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

  public static LongBinding selectLong(final Observable root, final Object... steps) {
    return new LongBinding() {
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
      protected long computeValue() {
        Object obj = helper.computeValue();

        return obj instanceof Number ? ((Number)obj).longValue() : 0;
      }
    };
  }

  public static DoubleBinding selectDouble(final Observable root, final Object... steps) {
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

  public static BooleanBinding selectBoolean(final Observable root, final Object... steps) {
    return new BooleanBinding() {
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
      protected boolean computeValue() {
        Object obj = helper.computeValue();

        return obj instanceof Boolean ? ((Boolean)obj).booleanValue() : false;
      }
    };
  }

  public static <T> ObjectBinding<T> select(final Observable root, final Object... steps) {
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
    private final Observable[] observables;
    private final Property[] properties;
    private final InvalidationListener listener = new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        binding.invalidate();
      }
    };
    private final WeakInvalidationListener observer = new WeakInvalidationListener(listener);
    private final Observable root;
    private final Object[] steps;
    private final Binding<?> binding;

    public Helper(Binding<?> binding, final Observable root, final Object... steps) {
      this.binding = binding;
      this.root = root;
      this.steps = expandSteps(root, steps);
      observables = new Observable[this.steps.length + 1];
      properties = new Property[this.steps.length];
    }

    private static Object[] expandSteps(Observable root, Object[] steps) {
      List<Object> expandedSteps = new ArrayList<>();
      int indicesRequired = root instanceof ObservableValue ? 0 : 1;

      for(int i = 0; i < steps.length; i++) {
        Object step = steps[i];

        if(indicesRequired-- <= 0) {
          if(!(step instanceof String)) {
            throw new IllegalArgumentException("expected String at step " + i + ": " + step);
          }

          indicesRequired = 0;

          for(String subStep : ((String)step).split("\\.")) {
            if(!subStep.matches("[_a-z][_A-Za-z0-9]*(\\[\\])?")) {
              throw new IllegalArgumentException("invalid step format at step " + i + ": " + step);
            }

            expandedSteps.add(subStep);

            if(subStep.endsWith("[]")) {
              indicesRequired++;
            }
          }
        }
        else {
          expandedSteps.add(step);
        }
      }

      if(indicesRequired > 0) {
        throw new IllegalArgumentException("insufficient indices specified in steps: missing " + indicesRequired + " indices");
      }

      return expandedSteps.toArray(new Object[expandedSteps.size()]);
    }

    private void unregisterListeners() {
      for(int index = 0; index < observables.length; index++) {
        if(observables[index] == null) {
          break;
        }
        observables[index].removeListener(this.observer);
        observables[index] = null;
      }
    }

    protected Object computeValue() {
      Observable observable = root;
      boolean indexedStep = false;

      for(int index = 0; index <= steps.length; index++) {
        observables[index] = observable;
        observable.addListener(observer);

        if(index == steps.length) {
          break;
        }

        Object value = observable instanceof ObservableValue ? ((ObservableValue<?>)observable).getValue() : observable;

        if(indexedStep && observable instanceof ObservableValue && !(value instanceof Observable)) {
          throw new RuntimeBindException("map or list expected at step " + (index - 1) + ": " + steps[index - 1] + " : " + observable);
        }

        indexedStep = steps[index] instanceof String && ((String)steps[index]).endsWith("[]") && observable instanceof ObservableValue ? true : false;

        try {
          if(properties[index] == null || !value.getClass().equals(properties[index].getBeanClass())) {
            properties[index] = new Property(value.getClass(), steps[index]);
          }

          observable = properties[index].getObservable(value);
        }
        catch(RuntimeBindException e) {
          throw e;
        }
        catch(RuntimeException e) {
          return null;
        }
      }

      return ((ObservableValue<?>)observable).getValue();
    }
  }

  private static class Property {
    private final Class<?> cls;
    private final Object name;

    private Method method;
    private Field field;

    public Property(Class<?> cls, Object name) {
      this.cls = cls;

      if(name instanceof String) {
        String s = (String)name;

        if(s.endsWith("[]")) {
          this.name = s.substring(0, s.length() - 2);
        }
        else {
          this.name = name;
        }
      }
      else {
        this.name = name;
      }
    }

    @SuppressWarnings("unchecked")
    public Observable getObservable(Object bean) {
      try {
        if(bean instanceof ObservableMap) {
          return Bindings.valueAt((ObservableMap<Object, ?>)bean, name);
        }

        if(bean instanceof ObservableList) {
          if(!(name instanceof Integer)) {
            throw new RuntimeBindException("index for ObservableList (" + bean + ") must be of type Integer: " + name.getClass());
          }

          int index = (Integer)name;

          if(index < 0) {
            throw new RuntimeBindException("index for ObservableList (" + bean + ") cannot be negative: " + index);
          }

          return Bindings.valueAt((ObservableList<?>)bean, index);
        }

        if(name instanceof String) {
          if(method == null && field == null) {
            try {
              method = cls.getMethod(name + "Property");
            }
            catch(NoSuchMethodException e) {
              field = cls.getField((String)name);
            }
          }

          return method != null ? (Observable)method.invoke(bean) : (Observable)field.get(bean);
        }

        throw new IllegalArgumentException("expected string: " + name);
      }
      catch(NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        e.printStackTrace();
        return null;
      }
    }

    public Class<?> getBeanClass() {
      return cls;
    }
  }
}
