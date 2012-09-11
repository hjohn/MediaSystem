package hs.mediasystem.framework;

import hs.mediasystem.enrich.EntityEnricher;
import hs.mediasystem.enrich.InstanceEnricher;
import hs.mediasystem.persist.Persister;
import hs.subtitle.DefaultThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Entity<T> {
  static final ThreadPoolExecutor PRIMARY_EXECUTOR = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
  static final ThreadPoolExecutor SECONDARY_EXECUTOR = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

  static {
    PRIMARY_EXECUTOR.setThreadFactory(new DefaultThreadFactory("EntityEnrichPrimary", Thread.NORM_PRIORITY - 2, true));
    SECONDARY_EXECUTOR.setThreadFactory(new DefaultThreadFactory("EntityEnrichSecondary", Thread.NORM_PRIORITY - 2, true));
  }

  private EntityFactory factory;

  private InstanceEnricher<T, Object> enricher;
  private Persister<T> persister;
  private boolean enricherCalled;

  public void setEntityFactory(EntityFactory factory) {
    this.factory = factory;
  }

  public <C extends Entity<?>> C create(Class<C> cls, Object... parameters) {
    if(factory == null) {
      throw new RuntimeException("No EntityFactory set for: " + this);
    }

    return factory.create(cls, parameters);
  }

  @SuppressWarnings("unchecked")
  public void setEnricher(InstanceEnricher<T, ? extends Object> enricher) {
    this.enricher = (InstanceEnricher<T, Object>)enricher;
  }

  public void setPersister(Persister<T> persister) {
    this.persister = persister;
  }

  private synchronized <P> P callEnricher(P value) {
    if(value == null && enricher != null && !enricherCalled) {
      enricherCalled = true;

      PRIMARY_EXECUTOR.execute(new EnrichTask<>(self(), enricher));
    }

    return value;
  }

  public static class EnrichTask<T, R> implements Runnable {
    private final InstanceEnricher<T, R> taskEnricher;
    private final T parent;

    public EnrichTask(T parent, InstanceEnricher<T, R> enricher) {
      this.parent = parent;
      this.taskEnricher = enricher;
    }

    @Override
    public void run() {
      final R result = taskEnricher.enrich(parent);

      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          taskEnricher.update(parent, result);
        }
      });
    }
  }

  private synchronized <P> void callEnricher() {
    callEnricher(null);
  }

  @SuppressWarnings("unchecked")
  private T self() {
    return (T)this;
  }

  protected <P, R> ObjectProperty<ObservableList<P>> list(final InstanceEnricher<T, R> enricher) {
    final ObservableList<P> observableArrayList = FXCollections.observableArrayList();

    return new SimpleObjectProperty<ObservableList<P>>(observableArrayList) {
      private boolean enricherCalled;

      @Override
      protected void invalidated() {
        if(persister != null) {
          persister.queueAsDirty(self());
          get();
        }
      }

      @Override
      public ObservableList<P> get() {
        if(enricher != null && !enricherCalled) {
          enricherCalled = true;

          PRIMARY_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
              final R result = enricher.enrich(self());

              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  enricher.update(self(), result);
                }
              });
            }
          });
        }

        return super.get();
      }
    };
  }

  protected <P> ObjectProperty<P> entity(final EntityEnricher<T, P> enricher) {
    return new SimpleObjectProperty<P>() {
      private boolean enricherCalled;

      @Override
      protected void invalidated() {
        if(persister != null) {
          persister.queueAsDirty(self());
          get();
        }
      }

      @Override
      public P get() {
        if(enricher != null && !enricherCalled) {
          enricherCalled = true;

          final SimpleObjectProperty<P> property = this;

          PRIMARY_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
              final P result = enricher.enrich(self());

              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  property.set(result);
                }
              });
            }
          });
          enricher.enrich(self());
        }
        return super.get();
      }
    };
  }

  protected <P> ObjectProperty<P> object() {
    return new SimpleObjectProperty<P>() {
      @Override
      protected void invalidated() {
        if(persister != null) {
          persister.queueAsDirty(self());
          get();
        }
      }

      @Override
      public P get() {
        return callEnricher(super.get());
      }
    };
  }

  protected StringProperty string(String initialValue) {
    return new SimpleStringProperty(initialValue) {
      @Override
      protected void invalidated() {
        if(persister != null) {
          persister.queueAsDirty(self());
          get();
        }
      }

      @Override
      public String get() {
        return callEnricher(super.get());
      }
    };
  }

  protected StringProperty string() {
    return string(null);
  }

  protected IntegerProperty integer() {
    return new SimpleIntegerProperty() {
      @Override
      protected void invalidated() {
        if(persister != null) {
          persister.queueAsDirty(self());
          get();
        }
      }

      @Override
      public int get() {
        callEnricher();
        return super.get();
      }
    };
  }

  protected DoubleProperty doubleProperty() {
    return new SimpleDoubleProperty() {
      @Override
      protected void invalidated() {
        if(persister != null) {
          persister.queueAsDirty(self());
          get();
        }
      }

      @Override
      public double get() {
        callEnricher();
        return super.get();
      }
    };
  }
}
