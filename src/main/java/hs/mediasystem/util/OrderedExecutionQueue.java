package hs.mediasystem.util;

import java.util.List;

import javafx.concurrent.Task;

public interface OrderedExecutionQueue<K> {
  void submit(K key, Task<?> task);
  void submitAll(K key, List<? extends Task<?>> runnables);
  List<Task<?>> removeAll(K key);

  void submitPending();
}