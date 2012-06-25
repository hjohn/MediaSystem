package hs.mediasystem.enrich;

import hs.mediasystem.enrich.EnrichCache.EnrichCacheTask;

public abstract class EnrichTask<T> {
  private final boolean isFast;

  private EnrichCacheTask delegationTask;
  private String title = "";
  private String message = "";
  private long workDone = -1;
  private long max = -1;

  public EnrichTask(boolean isFast) {
    this.isFast = isFast;
  }

  protected void updateTitle(String title) {
    this.title = title;

    updateDelegationTask();
  }

  protected void updateMessage(String message) {
    this.message = message;

    updateDelegationTask();
  }

  protected void updateProgress(long workDone, long max) {
    this.workDone = workDone;
    this.max = max;

    updateDelegationTask();
  }

  void setDelegationTask(EnrichCacheTask task) {
    this.delegationTask = task;

    updateDelegationTask();
  }

  private void updateDelegationTask() {
    if(delegationTask != null) {
      delegationTask.updateTitle(title);
      delegationTask.updateMessage(message);
      delegationTask.updateProgress(workDone, max);
    }
  }

  protected abstract T call() throws Exception;

  public String getTitle() {
    return title;
  }

  public boolean isFast() {
    return isFast;
  }

  @Override
  public String toString() {
    return "EnrichTask[" + (isFast ? "fast" : "slow") + "]";
  }
}
