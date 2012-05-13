package hs.mediasystem.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of an ExecutionQueue backed by a thread pool.
 */
public class ThreadPoolExecutionQueue implements ExecutionQueue {
  private final ThreadPoolExecutor executor;
  private final Map<Runnable, NodeList<Runnable>.Node> itemNodeMap = new HashMap<>();  // Quick key based Node lookup
  private final NodeList<Runnable> queue = new NodeList<>();  // Runnables waiting, head is most recently added/promoted
  private final int maxThreads;

  public ThreadPoolExecutionQueue(int maxThreads) {
    this.maxThreads = maxThreads;

    executor = new ThreadPoolExecutor(maxThreads, maxThreads, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>(), new NamedThreadFactory()) {
      @Override
      protected void afterExecute(Runnable r, Throwable t) {
        submitRunnablesIfSlotsAvailable();
      }
    };
  }

  @Override
  public void submit(Runnable runnable) {
    synchronized(queue) {
      itemNodeMap.put(runnable, queue.addHead(runnable));

      submitRunnablesIfSlotsAvailable();
    }
  }

  @Override
  public void promote(Runnable runnable) {
    synchronized(queue) {
      Runnable removedRunnable = queue.unlink(itemNodeMap.remove(runnable));

      if(removedRunnable != null) {
        submit(removedRunnable);
      }
    }
  }

  @Override
  public void cancel(Runnable runnable) {
    synchronized(queue) {
      queue.unlink(itemNodeMap.remove(runnable));
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
        Runnable runnable = queue.removeHead();
        itemNodeMap.remove(runnable);
        executor.execute(runnable);
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
}
