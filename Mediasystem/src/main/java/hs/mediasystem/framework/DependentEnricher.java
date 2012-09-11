package hs.mediasystem.framework;

import hs.mediasystem.enrich.InstanceEnricher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import javafx.application.Platform;

public class DependentEnricher<T, P> implements InstanceEnricher<T, Void> {
  private final List<Requirement<T>> requirements;
  private final List<EnrichCallback<P>> callbacks;
  private final FinishEnrichCallback<P> finishCallback;

  @SafeVarargs
  public DependentEnricher(List<EnrichCallback<P>> callbacks, FinishEnrichCallback<P> finishCallback, Requirement<T>... requirements) {
    this.finishCallback = finishCallback;
    this.callbacks = new ArrayList<>(callbacks);
    this.requirements = new ArrayList<>(Arrays.asList(requirements));
  }

  @Override
  public Void enrich(T parent) {
    for(Requirement<T> requirement : requirements) {
      requirement.attachListener(this, parent);
    }

    return null;
  }

  @Override
  public void update(final T parent, Void v) {
    final Object[] parameters = new Object[requirements.size()];
    int index = 0;

    for(Requirement<T> requirement : requirements) {
      Object value = requirement.getValue();

      if(value == null) {
        return;
      }

      parameters[index++] = value;
    }

    final Iterator<EnrichCallback<P>> iterator = callbacks.iterator();

    submit(iterator, parameters);
  }

  private void submit(final Iterator<EnrichCallback<P>> iterator, final Object[] parameters) {
    final EnrichCallback<P> callback = iterator.next();

    ThreadPoolExecutor executor = callback.equals(callbacks.get(0)) ? Entity.PRIMARY_EXECUTOR : Entity.SECONDARY_EXECUTOR;

    executor.execute(new Runnable() {
      @Override
      public void run() {
        final P result = callback.enrich(parameters);

        if(result == null && iterator.hasNext()) {
          submit(iterator, parameters);
        }
        else {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              finishCallback.update(result);
            }
          });
        }
      }
    });
  }
}