package hs.mediasystem.persist;

import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Persister {
  private final Map<Persistable, ScheduledFuture<?>> futures = new HashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private final ItemsDao itemsDao;

  @Inject
  public Persister(ItemsDao itemsDao) {
    this.itemsDao = itemsDao;

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

  public void queueAsDirty(final Persistable persistable) {
    synchronized(futures) {
      if(persistable instanceof MediaData) {
        ScheduledFuture<?> future = futures.get(persistable);

        if(future != null) {
          future.cancel(false);
          futures.remove(persistable);
        }

        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            System.out.println("[FINE] Persister - Persisting: " + persistable);

            itemsDao.updateMediaData((MediaData)persistable);

            synchronized(futures) {
              futures.remove(persistable);
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
}
