package hs.mediasystem.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javafx.application.Platform;

public class DependentEnricher<T, P> implements InstanceEnricher<T, Void> {
  private final List<Requirement<T>> requirements;
  private final List<EnrichCallback<P>> callbacks;
  private final FinishEnrichCallback<P> finishCallback;
  private final Class<?> endResultClass;

  private Object[] state = new String[] {"INACTIVE", null, null};

  @SafeVarargs
  public DependentEnricher(Class<?> endResultClass, List<EnrichCallback<P>> callbacks, FinishEnrichCallback<P> finishCallback, Requirement<T>... requirements) {
    this.endResultClass = endResultClass;
    this.finishCallback = finishCallback;
    this.callbacks = new ArrayList<>(callbacks);
    this.requirements = new ArrayList<>(Arrays.asList(requirements));
  }

  @Override
  public Void enrich(final T parent) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        state = new Object[] {"ATTACH LISTENERS", null, parent};

        for(Requirement<T> requirement : requirements) {
          requirement.attachListener(DependentEnricher.this, parent);
        }

        System.out.printf("Entity [LISTENERS]: %-20s REQ: %s BEAN: %s\n", endResultClass.getSimpleName(), requirements, parent);
      }
    });

    return null;
  }

  @Override
  public void update(final T parent, Void v) {
    final Object[] parameters = new Object[requirements.size()];
    int index = 0;

    System.out.printf("Entity [CHECK_REQ]: %-20s REQ: %s BEAN: %s\n", endResultClass.getSimpleName(), requirements, parent);

    for(Requirement<T> requirement : requirements) {
      Object value = requirement.getValue();

      if(value == null) {
        state = new Object[] {"REQUIREMENT WAIT", requirement, parent};
        return;
      }

      parameters[index++] = value;
    }

    System.out.printf("Entity [REQ_MET  ]: %-20s VALUES: %s BEAN: %s\n", endResultClass.getSimpleName(), Arrays.toString(parameters), parent);

    final Iterator<EnrichCallback<P>> iterator = callbacks.iterator();

    submit(iterator, parameters, parent);
  }

  private void submit(final Iterator<EnrichCallback<P>> iterator, final Object[] parameters, final T parent) {
    final EnrichCallback<P> callback = iterator.next();

    state = new Object[] {"ACTIVE", null, parent};

    boolean primary = callback.equals(callbacks.get(0));

    Entity.submit(primary, new EnrichmentRunnable(new Runnable() {
      @Override
      public void run() {
        final P result = callback.enrich(parameters);

        if(result == null && iterator.hasNext()) {
          submit(iterator, parameters, parent);
        }
        else {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              state = new Object[] {"FINISHING", null, parent};

              System.out.printf("Entity [COMPLETED]: %-20s RESULT: %s BEAN: %s\n", endResultClass.getSimpleName(), result, parent);

              finishCallback.update(result);

              state = new Object[] {"INACTIVE", null, parent};
            }
          });
        }
      }

      @Override
      public String toString() {
        return endResultClass.getSimpleName() + " for: " + parent;
      }
    }));
  }

  @Override
  public String toString() {
    return String.format("%-16s: %-20s %s", state[0], endResultClass.getSimpleName(), state[1] == null ? state[2] : state[1] + ": " + state[2]);
  }
}