package hs.mediasystem.entity;

import java.util.ArrayList;
import java.util.List;

public class EnricherBuilder<T, P> {
  private final Class<?> endResultClass;

  private List<Requirement<T>> requirements = new ArrayList<>();
  private List<EnrichCallback<P>> callbacks = new ArrayList<>();
  private FinishEnrichCallback<P> finishCallback;

  public EnricherBuilder(Class<?> endResultClass) {
    this.endResultClass = endResultClass;
  }

  public <R> EnricherBuilder<T, P> require(SimpleEntityProperty<R> property) {
    requirements.add(new Requirement<T>(property));

    return this;
  }

  public EnricherBuilder<T, P> enrich(EnrichCallback<P> callback) {
    this.callbacks.add(callback);

    return this;
  }

  public EnricherBuilder<T, P> finish(FinishEnrichCallback<P> callback) {
    this.finishCallback = callback;

    return this;
  }

  @SuppressWarnings("unchecked")
  public InstanceEnricher<T, Void> build() {
    return new DependentEnricher<>(endResultClass, callbacks, finishCallback, requirements.toArray(new Requirement[requirements.size()]));
  }

  public static class Parameters {
    private List<Object> list = new ArrayList<>();

    public void add(Object parameter) {
      list.add(parameter);
    }

    public Object get(int index) {
      return list.get(index);
    }
  }
}
