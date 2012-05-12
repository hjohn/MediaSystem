package hs.mediasystem.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of a KeyedExecutor backed by a thread pool.
 */
public class ThreadPoolKeyedExecutor implements KeyedExecutor {
  private final ThreadPoolExecutor executor;
  private final Map<Object, NodeList<QueueItem>.Node> itemNodeMap = new HashMap<>();  // Quick key based Node lookup
  private final NodeList<QueueItem> queue = new NodeList<>();  // Runnables waiting, head is most recently added/promoted
  private final int maxThreads;

  public ThreadPoolKeyedExecutor(int maxThreads) {
    this.maxThreads = maxThreads;

    executor = new ThreadPoolExecutor(maxThreads, maxThreads, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>(), new NamedThreadFactory()) {
      @Override
      protected void afterExecute(Runnable r, Throwable t) {
        submitRunnablesIfSlotsAvailable();
      }
    };
  }

  @Override
  public Object submit(Runnable runnable) {
    synchronized(queue) {
      Object key = new Object();

      itemNodeMap.put(key, queue.addHead(new QueueItem(key, runnable)));

      submitRunnablesIfSlotsAvailable();

      return key;
    }
  }

  @Override
  public void promote(Object key) {
    synchronized(queue) {
      QueueItem item = queue.unlink(itemNodeMap.remove(key));

      if(item != null) {
        itemNodeMap.put(key, queue.addHead(item));
      }

      submitRunnablesIfSlotsAvailable();
    }
  }

  @Override
  public void cancel(Object key) {
    synchronized(queue) {
      queue.unlink(itemNodeMap.remove(key));
    }
  }

  public int getActiveCount() {
    return executor.getActiveCount();
  }

  public long getTaskCount() {
    synchronized(queue) {
      return executor.getTaskCount() + itemNodeMap.size();
    }
  }

  public long getCompletedTaskCount() {
    return executor.getCompletedTaskCount();
  }

  private void submitRunnablesIfSlotsAvailable() {
    synchronized(queue) {
      int slotsAvailable = maxThreads - executor.getActiveCount() - executor.getQueue().size();

      while(slotsAvailable-- > 0 && !itemNodeMap.isEmpty()) {
        QueueItem item = queue.removeHead();
        itemNodeMap.remove(item.getKey());
        executor.execute(item.getRunnable());
      }
    }
  }

  private final class NamedThreadFactory implements ThreadFactory {
    private int threadNumber;

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, "ThreadPoolKeyedExecutor-thread-" + ++threadNumber);
      thread.setDaemon(true);
      return thread;
    }
  }

  private static class QueueItem {
    private final Object key;
    private final Runnable runnable;

    public QueueItem(Object key, Runnable runnable) {
      this.key = key;
      this.runnable = runnable;
    }

    public Object getKey() {
      return key;
    }

    public Runnable getRunnable() {
      return runnable;
    }
  }
}
