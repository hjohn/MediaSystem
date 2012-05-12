package hs.mediasystem.screens;

import javafx.concurrent.Task;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessagePaneExecutorService {
  private final GroupWorker groupService = new GroupWorker("Fetching metadata", 5);

  @Inject
  public MessagePaneExecutorService(ProgramController controller) {
    controller.registerWorker(groupService);
  }

  public void execute(Object key, Task<?> task) {
    groupService.submit(key, task);
  }

  public void promote(Object key) {
    groupService.promote(key);
  }
}
