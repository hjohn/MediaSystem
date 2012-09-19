package hs.mediasystem.persist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

@Singleton
public class PersistQueue {
  private final Map<Object, ScheduledFuture<?>> futures = new HashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public PersistQueue() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        scheduler.shutdown();

        try {
          if(scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
            return;
          }
        }
        catch(InterruptedException e) {
          System.out.println("[SEVERE] Persister - Interrupted during shutdown");
        }

        System.out.println("[SEVERE] Persister - Failed to shutdown Persister threads");

        List<Runnable> droppedRunnables = scheduler.shutdownNow();

        System.out.println("[SEVERE] Persister - Dropped tasks: " + droppedRunnables);
      }
    });
  }

  public void queueAsDirty(final Object persistable, final PersistTask task) {
    synchronized(futures) {
      ScheduledFuture<?> future = futures.get(persistable);

      if(future != null) {
        future.cancel(false);
        futures.remove(persistable);
      }

      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          try {
            System.out.println("[FINE] Persister - Persisting: " + persistable);

            task.persist();

            synchronized(futures) {
              futures.remove(persistable);
            }
          }
          catch(Exception e) {
            System.out.println("[WARN] Persister - Exception while persisting " + persistable + ": " + e);
            e.printStackTrace(System.out);
          }
        }

        @Override
        public String toString() {
          return "PersistTask[" + persistable + "]";
        }
      };

      futures.put(persistable, scheduler.schedule(runnable, 3, TimeUnit.SECONDS));
    }
  }
}
