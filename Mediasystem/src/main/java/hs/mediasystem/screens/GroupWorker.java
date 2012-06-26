package hs.mediasystem.screens;

import hs.mediasystem.util.TaskExecutor;
import hs.subtitle.DefaultThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class GroupWorker extends Service<Void> implements TaskExecutor {
  private final ThreadPoolExecutor executor;
  private final List<Task<?>> activeTasks = new ArrayList<>();  // Finished, executing and/or waiting tasks
  private final String title;

  private long startTaskCount;

  public GroupWorker(String title, int maxThreads) {
    this.title = title;
    this.executor = new ThreadPoolExecutor(maxThreads, maxThreads, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    this.executor.setThreadFactory(new DefaultThreadFactory("GroupWorker", Thread.NORM_PRIORITY - 2, true));

    new ActivityMonitorThread().start();
  }

  @Override
  public int getSlotsAvailable() {
    return executor.getMaximumPoolSize() - executor.getActiveCount() - executor.getQueue().size();
  }

  @Override
  public void submitTask(final Task<?> task) {
    synchronized(activeTasks) {
      activeTasks.add(task);
      executor.submit(task);
    }
  }

  @Override
  protected Task<Void> createTask() {
    return new Task<Void>() {
      {
        updateTitle(title + " (0/1)");
        updateProgress(0, 1);
      }

      @Override
      protected Void call() throws InterruptedException {
        for(;;) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              synchronized(activeTasks) {
                long completed = executor.getCompletedTaskCount() - startTaskCount;
                long total = activeTasks.size();
                double totalProgress = 0.0;
                double totalWork = 0.0;
                String message = "";

                for(Task<?> task : activeTasks) {
                  String title = task.getTitle();
                  double p = task.getProgress();

                  if(!task.isDone() && !title.isEmpty()) {
                    message += "• " + title + "\n";
                  }

                  if(p >= 0) {
                    totalProgress += p;
                  }
                  totalWork++;
                }

                updateTitle(title + " (" + completed + "/" + total +")");
                updateMessage(message);
                updateProgress((long)(totalProgress * 256), (long)(totalWork * 256));
              }
            }
          });

          synchronized(activeTasks) {
            if(executor.getCompletedTaskCount() == executor.getTaskCount()) {
              activeTasks.clear();
              startTaskCount = executor.getTaskCount();
              return null;
            }
          }

          Thread.sleep(100);
        }
      }
    };
  }

  private final class ActivityMonitorThread extends Thread {
    public ActivityMonitorThread() {
      setName("GroupWorker-activity-monitor");
      setDaemon(true);
    }

    @Override
    public void run() {
      try {
        for(;;) {
          if(!activeTasks.isEmpty()) {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                synchronized(activeTasks) {
                  if(!activeTasks.isEmpty() && !isRunning()) {
                    restart();
                  }
                }
              }
            });
          }

          Thread.sleep(100);
        }
      }
      catch(InterruptedException e) {
        System.out.println("[FINE] " + this + " interrupted");
      }
    }
  }
}