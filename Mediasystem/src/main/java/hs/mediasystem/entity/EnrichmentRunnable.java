package hs.mediasystem.entity;

import java.util.concurrent.atomic.AtomicLong;

public class EnrichmentRunnable implements Runnable, Comparable<EnrichmentRunnable> {
  private static final AtomicLong INSTANCE_COUNTER = new AtomicLong(0);

  private final Runnable runnable;
  private final int priority;
  private final long instanceNumber;

  private String state = "INACTIVE";
  private String threadName;

  public EnrichmentRunnable(int priority, Runnable runnable) {
    this.priority = priority;
    this.runnable = runnable;
    this.instanceNumber = INSTANCE_COUNTER.incrementAndGet();
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

  @Override
  public int compareTo(EnrichmentRunnable o) {
    int result = Integer.compare(o.priority, priority);  // higher priority first

    if(result == 0) {
      Long.compare(o.instanceNumber, instanceNumber);  // newer instance first (LIFO)
    }

    return result;
  }
}
