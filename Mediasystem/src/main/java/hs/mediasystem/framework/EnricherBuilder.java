package hs.mediasystem.framework;

import hs.mediasystem.enrich.InstanceEnricher;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.Property;
import javafx.collections.ObservableMap;

public class EnricherBuilder<T, P> {
  private List<Requirement<T>> requirements = new ArrayList<>();
  private List<EnrichCallback<P>> callbacks = new ArrayList<>();
  private FinishEnrichCallback<P> finishCallback;

  public <R> EnricherBuilder<T, P> require(Property<R> property) {
    requirements.add(new Requirement<T>(property));

    return this;
  }

  public <K, V> EnricherBuilder<T, P> require(final ObservableMap<K, V> property, final K key) {
    requirements.add(new Requirement<T>(property, key));

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
    return new DependentEnricher<>(callbacks, finishCallback, requirements.toArray(new Requirement[requirements.size()]));
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
