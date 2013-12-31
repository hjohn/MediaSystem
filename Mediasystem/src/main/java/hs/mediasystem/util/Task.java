package hs.mediasystem.util;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

/**
 * Tasks are {@link Runnable}s which consist of zero or more steps that execute one at a time in depth first
 * order.  Each task step is defined by a TaskRunnable, an {@link Executor} and optionally a conditional to
 * check before running the step.  Task steps can have further sub steps of their own.<p>
 *
 * If a task step has a condition, the condition will be checked in the context of the preceding step,
 * skipping the step completely if it evaluates to <code>false</code>.  This is mainly useful to prevent
 * unnecessary delays in the Task's execution when steps need to be enqueued on busy Executors, only to
 * exit immediately because its preconditions are not met.<p>
 *
 * To execute a Task simply pass it to an {@link Executor} or run it using a {@link Thread}.  The Task
 * will then run each of its steps in order, enqueing each step in turn with the appropriate Executor if
 * its conditions are met.<p>
 */
public class Task implements Runnable {
  private final Executor executor;
  private final BooleanSupplier condition;
  private final TaskRunnable runnable;

  private final Deque<Task> steps = new ConcurrentLinkedDeque<>();

  private Task parent;

  private Task(Executor executor, BooleanSupplier condition, TaskRunnable runnable) {
    this.executor = executor;
    this.condition = condition;
    this.runnable = runnable;
  }

  private Task(Executor executor, TaskRunnable runnable) {
    this(executor, null, runnable);
  }

  public Task(TaskRunnable runnable) {
    this(null, null, runnable);
  }

  public Task() {
    this(null, null);
  }

  @Override
  public final void run() {
    if(runnable != null) {
      runnable.run(this);
    }

    Task task = this;

    /*
     * Find the next step, traversing up the parent hierarchy if needed.
     */

    while(task != null) {
      Task nextTask = task.steps.poll();

      if(nextTask != null) {
        if(nextTask.executor != null && (nextTask.condition == null || nextTask.condition.getAsBoolean())) {
          nextTask.executor.execute(nextTask);
          break;
        }

        task = nextTask;
      }
      else {
        task = task.parent;
      }
    }
  }

  public final void addStep(Task task) {
    task.parent = this;
    steps.add(task);
  }

  public void addStep(Executor executor, BooleanSupplier condition, TaskRunnable step) {
    addStep(new Task(executor, condition, step));
  }

  public void addStep(Executor executor, TaskRunnable step) {
    addStep(new Task(executor, null, step));
  }

  public final boolean hasSteps() {
    return !steps.isEmpty();
  }

  /**
   * A Runnable which is passed the Task in which context it is running.  While
   * this TaskRunnable executes further steps can be added to the Task by use of
   * its {@link Task#addStep(Task)} methods.
   */
  public interface TaskRunnable {
    void run(Task currentTask);
  }
}
