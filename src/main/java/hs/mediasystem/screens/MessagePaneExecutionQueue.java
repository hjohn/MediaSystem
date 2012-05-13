package hs.mediasystem.screens;

import hs.mediasystem.util.ExecutionQueue;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessagePaneExecutionQueue implements ExecutionQueue {
  private final GroupWorker groupService = new GroupWorker("Fetching metadata", 5);

  @Inject
  public MessagePaneExecutionQueue(ProgramController controller) {
    controller.registerWorker(groupService);
  }

  @Override
  public void submit(Runnable runnable) {
    groupService.submit((Task<?>)runnable);
  }

  @Override
  public void promote(Runnable runnable) {
    groupService.promote(runnable);
  }

  @Override
  public void cancel(Runnable runnable) {
    groupService.cancel(runnable);
  }
}
