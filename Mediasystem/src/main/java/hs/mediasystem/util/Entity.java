package hs.mediasystem.util;

import hs.mediasystem.enrich.InstanceEnricher;
import hs.mediasystem.persist.Persister;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Entity<T> {
  private InstanceEnricher enricher;
  private Persister<T> persister;
  private boolean enricherCalled;

  public void setEnricher(InstanceEnricher enricher) {
    this.enricher = enricher;
  }

  public void setPersister(Persister<T> persister) {
    this.persister = persister;
  }

  private synchronized void callEnricher() {
    if(enricher != null && !enricherCalled) {
      enricherCalled = true;
      enricher.enrich(self());
    }
  }

  @SuppressWarnings("unchecked")
  private T self() {
    return (T)this;
  }

  protected <P> ObjectProperty<ObservableList<P>> list(final String listName) {
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
          enricher.enrich(self(), observableArrayList, listName);
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
        callEnricher();
        return super.get();
      }
    };
  }

  protected StringProperty string() {
    return new SimpleStringProperty() {
      @Override
      protected void invalidated() {
        if(persister != null) {
          persister.queueAsDirty(self());
          get();
        }
      }

      @Override
      public String get() {
        callEnricher();
        return super.get();
      }
    };
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

}
