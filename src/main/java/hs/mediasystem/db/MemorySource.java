package hs.mediasystem.db;

public class MemorySource<T> implements Source<T> {
  public static final MemorySource<?> NULL = new MemorySource<>(null);

  private final T data;

  public MemorySource(T data) {
    this.data = data;
  }

  @Override
  public T get() {
    return data;
  }
}
