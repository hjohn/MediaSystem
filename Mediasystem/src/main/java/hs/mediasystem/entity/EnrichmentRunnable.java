package hs.mediasystem.entity;

public class EnrichmentRunnable implements Runnable {
  private final Runnable runnable;

  private String state = "INACTIVE";
  private String threadName;

  public EnrichmentRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

  @Override
  public final void run() {
    state = "RUNNING ";
    threadName = Thread.currentThread().getName();

    runnable.run();

    state = "FINISHED";
    threadName = null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(state);
    builder.append(" : ");
    builder.append(runnable);
    if(threadName != null) {
      builder.append(" on " + threadName);
    }

    return builder.toString();
  }
}
