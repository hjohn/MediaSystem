package hs.mediasystem.util;

import java.util.concurrent.ThreadPoolExecutor;

import javafx.concurrent.Task;

public class TaskThreadPoolExecutor implements TaskExecutor {
  private final ThreadPoolExecutor executor;

  public TaskThreadPoolExecutor(ThreadPoolExecutor executor) {
    this.executor = executor;
  }

  @Override
  public void submitTask(Task<?> task) {
    executor.execute(task);
  }

  @Override
  public int getSlotsAvailable() {
    return executor.getMaximumPoolSize() - executor.getActiveCount() - executor.getQueue().size();
  }
}
