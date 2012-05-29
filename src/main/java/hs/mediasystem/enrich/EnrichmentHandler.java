package hs.mediasystem.enrich;

import hs.mediasystem.util.ExecutionQueue;
import hs.mediasystem.util.ThreadPoolExecutionQueue;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EnrichmentHandler {
  private final ExecutionQueue slowExecutionQueue;
  private final ExecutionQueue fastExecutionQueue = new ThreadPoolExecutionQueue(5);
  private final Map<TaskKey, EnrichTask<?>> enrichTasks = new HashMap<>();

  @Inject
  public EnrichmentHandler(ExecutionQueue executorQueue) {
    this.slowExecutionQueue = executorQueue;
  }

  public void enrich(EnrichTaskProvider<?> taskProvider, boolean bypassCache) {
    synchronized(enrichTasks) {
      EnrichTask<?> task = enrichTasks.get(taskProvider.getTaskKey());

      if(task == null) {
        if(bypassCache) {
          submitTask(taskProvider, taskProvider.getTask(bypassCache));
        }
        else {
          submitTask(taskProvider, taskProvider.getCachedTask());
        }
      }
      else {
        if(task.isFast()) {
          fastExecutionQueue.promote(task);
        }
        else {
          slowExecutionQueue.promote(task);
        }
      }
    }
  }

  private void submitTask(EnrichTaskProvider<?> taskProvider, EnrichTask<?> task) {
    synchronized(enrichTasks) {
      task.stateProperty().addListener(new TaskChangeListener(taskProvider, task));

      enrichTasks.put(taskProvider.getTaskKey(), task);

      System.out.println("[FINE] EnrichmentHandler: QUEUEING: " + taskProvider + ": " + task.getTitle());

      if(task.isFast()) {
        fastExecutionQueue.submit(task);
      }
      else {
        slowExecutionQueue.submit(task);
      }
    }
  }

  private final class TaskChangeListener implements ChangeListener<State> {
    private final EnrichTaskProvider<?> taskProvider;
    private final EnrichTask<?> task;

    private TaskChangeListener(EnrichTaskProvider<?> taskProvider, EnrichTask<?> task) {
      this.taskProvider = taskProvider;
      this.task = task;
    }

    @Override
    public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
      if(newValue == State.FAILED || newValue == State.CANCELLED || newValue == State.SUCCEEDED) {
        Object taskResult = task.getValue();
        TaskKey key = taskProvider.getTaskKey();

        System.out.println("[FINE] EnrichmentHandler: " + newValue + ": " + taskProvider + ": " + task.getTitle());

        if(newValue == State.FAILED) {
          System.out.println("[WARN] EnrichmentHandler.TaskChangeListener.changed() - Worker " + task.getTitle() + " failed with exception: " + task.getException());
          task.getException().printStackTrace(System.out);
          key.getKey().getEnrichCache().insert(key.getKey(), EnrichmentState.ENRICHMENT_FAILED, key.getEnrichableClass(), null);
        }

        if(newValue == State.SUCCEEDED && taskResult != null) {
          key.getKey().getEnrichCache().insert(key.getKey(), EnrichmentState.ENRICHED, key.getEnrichableClass(), taskResult);
        }

        synchronized(enrichTasks) {

          /*
           * The removal of the task must be after the result was added to Cache as otherwise another enrich might be triggered.
           * The removal (of a fast EnrichTask) must also be before the new (slow) EnrichTask is submitted as otherwise the new task
           * will be removed too early by this code.
           */

          enrichTasks.remove(key);
        }

        if(newValue == State.SUCCEEDED && taskResult == null) {
          if(task.isFast()) {
            submitTask(taskProvider, taskProvider.getTask(false));
          }
        }
      }
    }
  }
}
