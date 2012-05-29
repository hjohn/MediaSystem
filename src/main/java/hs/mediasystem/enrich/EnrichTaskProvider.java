package hs.mediasystem.enrich;

public interface EnrichTaskProvider<T> {
  EnrichTask<T> getCachedTask();
  EnrichTask<T> getTask(boolean bypassCache);
  TaskKey getTaskKey();
}
