package hs.mediasystem.screens;

import hs.mediasystem.util.LifoBlockingDeque;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessagePaneExecutorService {
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>(), new ThreadFactory() {
    private int threadNumber;

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, "MessagePaneExecutorService-thread-" + ++threadNumber);
      thread.setDaemon(true);
      return thread;
    }
  });

  private final GroupWorker groupService = new GroupWorker();

  @Inject
  public MessagePaneExecutorService(ProgramController controller) {
    controller.registerWorker(groupService);
  }

  public synchronized void execute(final Task<?> task) {
    groupService.submit(task);
  }

  private class GroupWorker extends Service<Void> {
    private final List<Task<?>> activeTasks = new ArrayList<>();

    private long startTaskCount;

    public GroupWorker() {
      Thread thread = new Thread() {
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
      };

      thread.setName("GroupWorker-activity-monitor");
      thread.setDaemon(true);
      thread.start();
    }

    public void submit(final Task<?> task) {
      synchronized(activeTasks) {
        activeTasks.add(task);
        executor.execute(task);
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

    @Override
    protected Task<Void> createTask() {
      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          for(;;) {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                long completed = executor.getCompletedTaskCount() - startTaskCount;
                long total = executor.getTaskCount() - startTaskCount;
                double totalProgress = 0.0;
                double totalWork = 0.0;
                String title = "";
                String message = "";

                synchronized(activeTasks) {
                  for(Task<?> task : activeTasks) {
                    String m = task.getMessage();
                    double p = task.getProgress();

                    if(!task.isDone() && !m.isEmpty()) {
                      title = task.getTitle();
                      message += "• " + task.getMessage() + "\n";
                    }

                    if(p >= 0) {
                      totalProgress += task.getProgress();
                    }
                    totalWork++;
                  }
                }

                updateTitle(title + " (" + completed + "/" + total +")");
                updateMessage(message);
                updateProgress((long)(totalProgress * 256), (long)(totalWork * 256));
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
  }
}
