package hs.mediasystem;

import java.util.concurrent.LinkedBlockingDeque;

public class LifoBlockingDeque<T> extends LinkedBlockingDeque<T> {

  public LifoBlockingDeque(int capacity) {
    super(capacity);
  }
  
  @Override
  public boolean offer(T t) {
    return super.offerFirst(t);
  }

  @Override
  public T remove() {
    return super.removeFirst();
  }
}