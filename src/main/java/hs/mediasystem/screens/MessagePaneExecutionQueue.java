package hs.mediasystem.screens;

import hs.mediasystem.util.ExecutionQueue;
import hs.mediasystem.util.TaskExecutor;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessagePaneExecutionQueue implements ExecutionQueue, TaskExecutor {
  private final GroupWorker groupService = new GroupWorker("Fetching metadata", 5);

  @Inject
  public MessagePaneExecutionQueue(ProgramController controller) {
    controller.registerWorker(groupService);
  }

  @Override
  public void submit(Runnable runnable) {
    groupService.submitTask((Task<?>)runnable);
  }

  @Override
  public void promote(Runnable runnable) {
    groupService.promote(runnable);
  }

  @Override
  public void cancel(Runnable runnable) {
    groupService.cancel(runnable);
  }

  @Override
  public void submitTask(Task<?> task) {
    groupService.submitTask(task);
  }

  @Override
  public int getSlotsAvailable() {
    return groupService.getSlotsAvailable();
  }
}
