package hs.mediasystem.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javafx.concurrent.Task;

/**
 * An implementation of an ExecutionQueue backed by a thread pool.
 */
public class OrderedExecutionQueueExecutor<K> implements OrderedExecutionQueue<K> {
  private final TaskExecutor executor;

  private NavigableMap<K, List<Task<?>>> queue;

  public OrderedExecutionQueueExecutor(Comparator<K> comparator, TaskExecutor executor) {
    this.executor = executor;
    this.queue = new TreeMap<>(comparator);
  }

  @Override
  public void submitPending() {
    synchronized(executor) {
      int slotsAvailable = executor.getSlotsAvailable();

      while(slotsAvailable-- > 0 && !queue.isEmpty()) {
        executor.submitTask(getHead());
      }
    }
  }

  @Override
  public void submit(K key, Task<?> task) {
    synchronized(executor) {
      List<Task<?>> set = queue.get(key);

      if(set == null) {
        set = new ArrayList<>();
        queue.put(key, set);
      }

      set.add(task);

      submitPending();
    }
  }

  @Override
  public void submitAll(K key, List<? extends Task<?>> tasks) {
    synchronized(executor) {
      for(Task<?> task : tasks) {
        submit(key, task);
      }
    }
  }

  @Override
  public List<Task<?>> removeAll(K key) {
    synchronized(queue) {
      return queue.remove(key);
    }
  }

  private Task<?> getHead() {
    synchronized(executor) {
      K firstKey = queue.firstKey();

      List<Task<?>> list = queue.get(firstKey);

      Task<?> task = list.remove(list.size() - 1);

      if(list.isEmpty()) {
        queue.remove(firstKey);
      }

      return task;
    }
  }
}
