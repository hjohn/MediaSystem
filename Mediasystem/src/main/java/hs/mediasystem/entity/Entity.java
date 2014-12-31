package hs.mediasystem.entity;

import java.util.function.BiFunction;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Entity {
  private static final Logger LOGGER = Logger.getLogger(Entity.class.getName());
  private static final EnrichTrigger DEFAULT_ENRICH_TRIGGER = EnrichTrigger.IS_NOT_SET;

  public enum EnrichTrigger {
    NEVER((v, s) -> false),
    IS_NULL((v, s) -> v == null),
    IS_NOT_SET((v, s) -> !s),
    ALWAYS((v, s) -> true);

    private final BiFunction<Object, Boolean, Boolean> condition;

    EnrichTrigger(BiFunction<Object, Boolean, Boolean> condition) {
      this.condition = condition;
    }

    public boolean shouldEnrich(Object value, boolean setCalled) {
      return condition.apply(value, setCalled);
    }
  }

  private EntityContext context;
  private boolean enrichTriggered;

  public EntityContext getContext() {
    return context;
  }

  void setContext(EntityContext context) {
    this.context = context;
  }

  private void queueAsDirty(Property<?> property) {
    if(context != null) {
      context.queueAsDirty(this, property);
    }
  }

  private void checkForEnrichment(Property<?> property, Object value, boolean setCalled, EnrichTrigger enrichTrigger) {
    if(!enrichTriggered && context != null && enrichTrigger.shouldEnrich(value, setCalled || property.isBound())) {
      enrichTriggered = true;
      context.queueForEnrichment(this);

      LOGGER.fine("Enrich Triggered by access (" + enrichTrigger + ") of: " + getClass().getName() + "." + property.getName() + ": " + this);
    }
  }

  private <P extends Entity, E extends Entity> void queueListProvide(P parentEntity, Class<E> itemClass, ObjectProperty<ObservableList<E>> property) {
    context.ensureRunsOnUpdateThread();
    checkForEnrichment(property, null, false, EnrichTrigger.IS_NULL);

    getContext().queueListProvide(parentEntity, itemClass, property);
  }

  protected <P> ObjectProperty<P> object(String name, EnrichTrigger enrichTrigger) {
    return new SimpleObjectProperty<P>(this, name) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public P get() {
        P value = super.get();
        checkForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(P newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected <P> ObjectProperty<P> object(String name) {
    return object(name, DEFAULT_ENRICH_TRIGGER);
  }

  protected StringProperty stringProperty(String name, String initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleStringProperty(Entity.this, name, initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public String get() {
        String value = super.get();
        checkForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(String newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected StringProperty stringProperty(String name, String initialValue) {
    return stringProperty(name, initialValue, DEFAULT_ENRICH_TRIGGER);
  }

  protected StringProperty stringProperty(String name, EnrichTrigger enrichTrigger) {
    return stringProperty(name, null, enrichTrigger);
  }

  protected StringProperty stringProperty(String name) {
    return stringProperty(name, null, DEFAULT_ENRICH_TRIGGER);
  }

  protected IntegerProperty integerProperty(String name, int initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleIntegerProperty(Entity.this, name, initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public int get() {
        int value = super.get();
        checkForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(int newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected IntegerProperty integerProperty(String name, EnrichTrigger enrichTrigger) {
    return integerProperty(name, 0, enrichTrigger);
  }

  protected IntegerProperty integerProperty(String name) {
    return integerProperty(name, 0, DEFAULT_ENRICH_TRIGGER);
  }

  protected LongProperty longProperty(String name, long initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleLongProperty(Entity.this, name, initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public long get() {
        long value = super.get();
        checkForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(long newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected LongProperty longProperty(String name, EnrichTrigger enrichTrigger) {
    return longProperty(name, 0L, enrichTrigger);
  }

  protected LongProperty longProperty(String name) {
    return longProperty(name, 0L, DEFAULT_ENRICH_TRIGGER);
  }

  protected BooleanProperty booleanProperty(String name, boolean initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleBooleanProperty(Entity.this, name, initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public boolean get() {
        boolean value = super.get();
        checkForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(boolean newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected BooleanProperty booleanProperty(String name, EnrichTrigger enrichTrigger) {
    return booleanProperty(name, false, enrichTrigger);
  }

  protected BooleanProperty booleanProperty(String name) {
    return booleanProperty(name, false, DEFAULT_ENRICH_TRIGGER);
  }

  protected FloatProperty floatProperty(String name, float initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleFloatProperty(Entity.this, name, initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public float get() {
        float value = super.get();
        checkForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(float newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected FloatProperty floatProperty(String name, EnrichTrigger enrichTrigger) {
    return floatProperty(name, 0.0f, enrichTrigger);
  }

  protected FloatProperty floatProperty(String name) {
    return floatProperty(name, 0.0f, DEFAULT_ENRICH_TRIGGER);
  }

  protected DoubleProperty doubleProperty(String name, double initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleDoubleProperty(Entity.this, name, initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public double get() {
        double value = super.get();
        checkForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(double newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected DoubleProperty doubleProperty(String name, EnrichTrigger enrichTrigger) {
    return doubleProperty(name, 0.0, enrichTrigger);
  }

  protected DoubleProperty doubleProperty(String name) {
    return doubleProperty(name, 0.0, DEFAULT_ENRICH_TRIGGER);
  }

  protected <E extends Entity> ObjectProperty<ObservableList<E>> list(String name, Class<E> itemClass) {
    return new SimpleObjectProperty<ObservableList<E>>(Entity.this, name) {
      private boolean enricherCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public ObservableList<E> get() {
        ObservableList<E> value = super.get();

        if(value == null && !enricherCalled && getContext() != null) {
          enricherCalled = true;
          queueListProvide(Entity.this, itemClass, this);
        }

        return value;
      }
    };
  }
}
