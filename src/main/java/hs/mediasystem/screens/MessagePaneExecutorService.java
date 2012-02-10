package hs.mediasystem.screens;

import hs.mediasystem.util.LifoBlockingDeque;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessagePaneExecutorService {
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>());
  private final GroupWorker groupService = new GroupWorker();

  @Inject
  public MessagePaneExecutorService(ProgramController controller) {
    controller.registerWorker(groupService);
  }

  public synchronized void execute(Task<?> task) {
    groupService.queue.offer(task);

    if(!groupService.isRunning()) {
      groupService.restart();
    }
  }

  private class GroupWorker extends Service<Void> {
    private final Queue<Task<?>> queue = new ConcurrentLinkedQueue<>();

    private long submittedTaskCount;

    @Override
    protected Task<Void> createTask() {
      final long startTaskCount = submittedTaskCount;

      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          final List<Task<?>> tasks = new ArrayList<>();

          for(;;) {
            synchronized(tasks) {
              for(;;) {
                Task<?> newTask = queue.poll();

                if(newTask == null) {
                  break;
                }

                tasks.add(newTask);
                submittedTaskCount++;
                executor.execute(newTask);
              }
            }

            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                long completed = executor.getCompletedTaskCount() - startTaskCount;
                long total = submittedTaskCount - startTaskCount;
                double totalProgress = 0.0;
                double totalWork = 0.0;
                String title = "";
                String message = "";

                synchronized(tasks) {
                  for(Task<?> task : tasks) {
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

            if(executor.getCompletedTaskCount() == submittedTaskCount) {
              break;
            }

            Thread.sleep(100);
          }

          return null;
        }
      };
    }
  }
}
