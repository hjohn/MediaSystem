package hs.mediasystem.beans;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * A property that takes on the value of an observed property when it has
 * not changed for a while.
 */
public class DelayedObjectProperty<T> extends SimpleObjectProperty<T> {
  private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

  private ScheduledFuture<?> propagationFuture; // Must be on JavaFX thread to access

  public DelayedObjectProperty(ObjectProperty<T> observedProperty, long propagationMillis) {
    observedProperty.addListener(new ChangeListener<T>() {
      @Override
      public void changed(ObservableValue<? extends T> observable, T oldValue, T value) {
        if(propagationFuture != null) {
          propagationFuture.cancel(true);
        }

        propagationFuture = SCHEDULED_EXECUTOR_SERVICE.schedule(
          () -> propagateValue(new WeakReference<>(DelayedObjectProperty.this), value),
          propagationMillis,
          TimeUnit.MILLISECONDS
        );
      }
    });
  }

  private static <T> void propagateValue(WeakReference<DelayedObjectProperty<T>> propertyRef, T value) {
    DelayedObjectProperty<T> property = propertyRef.get();

    if(property != null) {
      Platform.runLater(() -> property.set(value));
    }
  }
}
