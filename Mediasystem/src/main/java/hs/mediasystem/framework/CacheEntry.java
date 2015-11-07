package hs.mediasystem.framework;

import java.time.LocalDateTime;

public class CacheEntry<T> {
  private final T data;
  private final LocalDateTime creationTime;

  public CacheEntry(T data, LocalDateTime creationTime) {
    this.data = data;
    this.creationTime = creationTime;
  }

  public T getData() {
    return data;
  }

  public LocalDateTime getCreationTime() {
    return creationTime;
  }
}
