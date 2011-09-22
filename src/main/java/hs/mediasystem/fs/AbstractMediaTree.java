package hs.mediasystem.fs;

import hs.mediasystem.LifoBlockingDeque;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.screens.movie.ItemUpdate;
import hs.models.events.ListenerList;
import hs.models.events.Notifier;
import hs.sublight.DefaultThreadFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMediaTree implements MediaTree {
  private final Notifier<ItemUpdate> itemUpdateNotifier = new Notifier<>();
  private final ThreadPoolExecutor executor;
  private final ItemEnricher itemEnricher;
  
  public AbstractMediaTree(ItemEnricher itemEnricher) {
    this.itemEnricher = itemEnricher;
    
    executor = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>(10)) {
      @Override
      protected void afterExecute(Runnable r, Throwable t) {
        itemUpdateNotifier.notifyListeners(new ItemUpdate(((ItemUpdater)r).getNamedItem()));
      }
    };
    
    /*
     * Requirements:
     * - New tasks take priority over older tasks
     * - If queue is full, the oldest task is rejected in favor of a new task
     * 
     * Using a LIFO queue means tasks that are submitted to the executor are added at the head of
     * the queue.  The standard DiscardOldestPolicy will discard from the head of the queue, but
     * this contains the newest tasks when using a LIFO queue.  So a new LifoDiscardOldestPolicy
     * is needed, implemented below:
     */
    
    executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
      @Override
      public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if(!e.isShutdown()) {
          ((BlockingDeque<Runnable>)e.getQueue()).pollLast();
          e.execute(r);
        }
      }
    });
    
    executor.setThreadFactory(new DefaultThreadFactory("MediaTree", Thread.NORM_PRIORITY, true));
  }
  
  @Override
  public void triggerItemUpdate(final NamedItem namedItem) {
    if(namedItem.getPath() != null) {
      executor.execute(new ItemUpdater(namedItem) {
        @Override
        public void run() {
          try {
            itemEnricher.enrichItem(namedItem.getItem());
          }
          catch(ItemNotFoundException e) {
          }
        }
      });
    }
  }

  @Override
  public ListenerList<ItemUpdate> onItemUpdate() {
    return itemUpdateNotifier.getListenerList();
  }
  
  private static abstract class ItemUpdater implements Runnable {
    private final NamedItem item;

    public ItemUpdater(NamedItem item) {
      this.item = item;
    }
    
    public NamedItem getNamedItem() {
      return item;
    }
  }
}
