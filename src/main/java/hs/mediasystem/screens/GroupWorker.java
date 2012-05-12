package hs.mediasystem.screens;

import hs.mediasystem.util.ThreadPoolKeyedExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class GroupWorker extends Service<Void> {
  private final ThreadPoolKeyedExecutor executor;
  private final List<Task<?>> activeTasks = new ArrayList<>();  // Finished, executing and/or waiting tasks
  private final Map<Object, Object> keyMap = new HashMap<>();
  private final String title;

  private long startTaskCount;

  public GroupWorker(String title, int maxThreads) {
    this.title = title;
    this.executor = new ThreadPoolKeyedExecutor(maxThreads);

    new ActivityMonitorThread().start();
  }

  public void submit(final Object key, final Task<?> task) {
    synchronized(activeTasks) {
      activeTasks.add(task);
      keyMap.put(key, executor.submit(task));
    }

    task.stateProperty().addListener(new ChangeListener<State>() {
      @Override
      public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
        if(newValue == State.FAILED) {
          System.out.println("[WARN] Worker " + task.getTitle() + " failed with exception: " + task.getException());
          task.getException().printStackTrace(System.out);
        }
      }
    });
  }

  public void promote(Object key) {
    executor.promote(keyMap.get(key));
  }

  public void cancel(Object key) {
    executor.cancel(keyMap.get(key));
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
              keyMap.clear();
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