package hs.mediasystem.db;

public class MemorySource<T> implements Source<T> {
  private final T data;

  public MemorySource(T data) {
    assert data != null;

    this.data = data;
  }

  @Override
  public T get() {
    return data;
  }
}
