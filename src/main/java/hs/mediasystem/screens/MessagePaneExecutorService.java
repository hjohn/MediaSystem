package hs.mediasystem.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessagePaneExecutorService {
  private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
  private final GroupWorker groupService = new GroupWorker();

  @Inject
  public MessagePaneExecutorService(ProgramController controller) {
    controller.registerWorker(groupService);
  }

  public synchronized void execute(Task<?> task) {
    groupService.queue.add(task);

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
          List<Task<?>> tasks = new ArrayList<>();

          for(;;) {
            for(;;) {
              Task<?> newTask = queue.poll();

              if(newTask == null) {
                break;
              }

              tasks.add(newTask);
              submittedTaskCount++;
              executor.execute(newTask);
            }

            long completed = executor.getCompletedTaskCount() - startTaskCount;
            long total = submittedTaskCount - startTaskCount;

            String title = "";
            String message = "";

            for(Task<?> task : tasks) {
              String m = task.getMessage();
              if(!task.isDone() && !m.isEmpty()) {
                title = task.getTitle();
                message += "• " + task.getMessage() + "\n";
              }
            }

            updateTitle(title + " (" + completed + "/" + total +")");
            updateMessage(message);
            updateProgress(completed, submittedTaskCount - startTaskCount);

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
