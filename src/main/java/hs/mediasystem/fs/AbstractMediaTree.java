package hs.mediasystem.fs;

import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.framework.MediaTree;

public abstract class AbstractMediaTree implements MediaTree {
//  private static final ThreadPoolExecutor EXECUTOR;

  private final ItemEnricher itemEnricher;

//  static {
//    EXECUTOR = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>(100)) {
//      @Override
//      protected void afterExecute(Runnable r, Throwable t) {
//        ((ItemUpdater)r).notifyListeners();
//      }
//    };
//
//    /*
//     * Requirements:
//     * - New tasks take priority over older tasks
//     * - If queue is full, the oldest task is rejected in favor of a new task
//     *
//     * Using a LIFO queue means tasks that are submitted to the executor are added at the head of
//     * the queue.  The standard DiscardOldestPolicy will discard from the head of the queue, but
//     * this contains the newest tasks when using a LIFO queue.  So a new LifoDiscardOldestPolicy
//     * is needed, implemented below:
//     */
//
//    EXECUTOR.setRejectedExecutionHandler(new RejectedExecutionHandler() {
//      @Override
//      public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
//        if(!e.isShutdown()) {
//          ((BlockingDeque<Runnable>)e.getQueue()).pollLast();
//          e.execute(r);
//        }
//      }
//    });
//
//    EXECUTOR.setThreadFactory(new DefaultThreadFactory("MediaTree", Thread.NORM_PRIORITY - 1, true));
//  }

  public AbstractMediaTree(ItemEnricher itemEnricher) {
    this.itemEnricher = itemEnricher;
  }

  @Override
  public void enrichItem(final NamedItem namedItem) {
    try {
      itemEnricher.enrichItem(namedItem.getItem());
    }
    catch(ItemNotFoundException e) {
      System.out.println("Item Enrichment failed: " + e);
    }
  }
}
