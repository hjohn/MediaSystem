package hs.mediasystem.screens;

import hs.mediasystem.util.TaskExecutor;
import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessagePaneTaskExecutor implements TaskExecutor {
  private final GroupWorker groupService = new GroupWorker("Fetching metadata", 5);

  @Inject
  public MessagePaneTaskExecutor(ProgramController controller) {
    controller.registerWorker(groupService);
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
