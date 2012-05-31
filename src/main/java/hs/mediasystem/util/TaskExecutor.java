package hs.mediasystem.util;

import javafx.concurrent.Task;

public interface TaskExecutor {
  void submitTask(Task<?> task);
  int getSlotsAvailable();
}
