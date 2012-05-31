package hs.mediasystem.enrich;

import javafx.concurrent.Task;

public abstract class EnrichTask<T> extends Task<EnrichmentResult<T>> {
  private final boolean isFast;

  public EnrichTask(boolean isFast) {
    this.isFast = isFast;
  }

  public boolean isFast() {
    return isFast;
  }

  @Override
  public String toString() {
    return "EnrichTask[" + (isFast ? "fast" : "slow") + "]";
  }
}
