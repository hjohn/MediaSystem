package hs.mediasystem.util;

import hs.subtitle.DefaultThreadFactory;

import java.util.concurrent.ThreadPoolExecutor;

import javafx.concurrent.Task;

public class TaskThreadPoolExecutor implements TaskExecutor {
  private final ThreadPoolExecutor executor;

  public TaskThreadPoolExecutor(ThreadPoolExecutor executor) {
    this.executor = executor;
    this.executor.setThreadFactory(new DefaultThreadFactory("TaskThreadPoolExecutor", Thread.NORM_PRIORITY - 2, true));
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
