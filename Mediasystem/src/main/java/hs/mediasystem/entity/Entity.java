package hs.mediasystem.entity;

import java.util.function.BiFunction;

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
  private LoadState loadState = LoadState.SPARSE;
  private boolean queuedForEnrichment;

  void setContext(EntityContext context) {
    this.context = context;
  }

  public EntityContext getContext() {
    return context;
  }

  public LoadState getLoadState() {
    return loadState;
  }

  public void setLoadState(LoadState loadState) {
    this.loadState = loadState;
  }

  void clearQueuedForEnrichment() {
    queuedForEnrichment = false;
  }

  private void setQueuedForEnrichment() {
    queuedForEnrichment = true;
  }

  private boolean isQueuedForEnrichment() {
    return queuedForEnrichment;
  }

  private void queueAsDirty(Property<?> property) {
    if(context != null) {
      context.queueAsDirty(this, property);
    }
  }

  private void queueForEnrichment(Property<?> property, Object value, boolean setCalled, EnrichTrigger enrichTrigger) {
    if(context != null && !isQueuedForEnrichment() && enrichTrigger.shouldEnrich(value, setCalled || property.isBound())) {
      System.out.println("[FINE] Enrich Triggered by access (" + enrichTrigger + ") of: " + getClass().getName() + "." + property.getName());

      context.ensureRunsOnUpdateThread();

      setQueuedForEnrichment();
      context.queueForEnrichment(this);
    }
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
        queueForEnrichment(this, value, setCalled, enrichTrigger);
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
        queueForEnrichment(this, value, setCalled, enrichTrigger);
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

  protected StringProperty stringProperty(EnrichTrigger enrichTrigger) {
    return stringProperty(null, null, enrichTrigger);
  }

  protected StringProperty stringProperty(String name) {
    return stringProperty(name, null, DEFAULT_ENRICH_TRIGGER);
  }

//  protected StringProperty stringProperty() {
//    return stringProperty(null, null, DEFAULT_ENRICH_TRIGGER);
//  }

  protected IntegerProperty integerProperty(int initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleIntegerProperty(initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public int get() {
        int value = super.get();
        queueForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(int newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected IntegerProperty integerProperty(EnrichTrigger enrichTrigger) {
    return integerProperty(0, enrichTrigger);
  }

  protected IntegerProperty integerProperty() {
    return integerProperty(0, DEFAULT_ENRICH_TRIGGER);
  }

  protected LongProperty longProperty(long initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleLongProperty(initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public long get() {
        long value = super.get();
        queueForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(long newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected LongProperty longProperty(EnrichTrigger enrichTrigger) {
    return longProperty(0L, enrichTrigger);
  }

  protected LongProperty longProperty() {
    return longProperty(0L, DEFAULT_ENRICH_TRIGGER);
  }

  protected BooleanProperty booleanProperty(boolean initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleBooleanProperty(initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public boolean get() {
        boolean value = super.get();
        queueForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(boolean newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected BooleanProperty booleanProperty(EnrichTrigger enrichTrigger) {
    return booleanProperty(false, enrichTrigger);
  }

  protected BooleanProperty booleanProperty() {
    return booleanProperty(false, DEFAULT_ENRICH_TRIGGER);
  }

  protected FloatProperty floatProperty(float initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleFloatProperty(initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public float get() {
        float value = super.get();
        queueForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(float newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected FloatProperty floatProperty(EnrichTrigger enrichTrigger) {
    return floatProperty(0.0f, enrichTrigger);
  }

  protected FloatProperty floatProperty() {
    return floatProperty(0.0f, DEFAULT_ENRICH_TRIGGER);
  }

  protected DoubleProperty doubleProperty(double initialValue, EnrichTrigger enrichTrigger) {
    return new SimpleDoubleProperty(initialValue) {
      private boolean setCalled;

      @Override
      protected void invalidated() {
        queueAsDirty(this);
      }

      @Override
      public double get() {
        double value = super.get();
        queueForEnrichment(this, value, setCalled, enrichTrigger);
        return value;
      }

      @Override
      public void set(double newValue) {
        setCalled = true;
        super.set(newValue);
      }
    };
  }

  protected DoubleProperty doubleProperty(EnrichTrigger enrichTrigger) {
    return doubleProperty(0.0, enrichTrigger);
  }

  protected DoubleProperty doubleProperty() {
    return doubleProperty(0.0, DEFAULT_ENRICH_TRIGGER);
  }

  protected <E extends Entity> ObjectProperty<ObservableList<E>> list(Class<E> itemClass) {
    return new SimpleObjectProperty<ObservableList<E>>() {
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
          getContext().queueListProvide(Entity.this, itemClass, this);
        }

        return value;
      }
    };
  }
}
