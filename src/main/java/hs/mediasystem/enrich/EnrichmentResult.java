package hs.mediasystem.enrich;

public class EnrichmentResult<T> {
  private final T primaryResult;
  private final Object[] intermediateResults;

  public EnrichmentResult(T primaryResult, Object... intermediateResults) {
    this.primaryResult = primaryResult;
    this.intermediateResults = intermediateResults;
  }

  public T getPrimaryResult() {
    return primaryResult;
  }

  public Object[] getIntermediateResults() {
    return intermediateResults;
  }
}
