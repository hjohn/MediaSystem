package hs.mediasystem.util;

/**
 * An object that queues {@link Runnable} tasks for execution and provides means of manipulating the queue.
 *
 * Generally, implementors of this interface will only have a limited number of threads available for executing
 * the submitted tasks while the rest is queued.  As long as tasks are queued they can be manipulated.
 */
public interface ExecutionQueue {

  /**
   * Submits a new Runnable for future execution.
   *
   * @param runnable a Runnable
   */
  void submit(Runnable runnable);

  /**
   * Promotes the given Runnable to the head of the queue.  This makes this Runnable
   * the first to be executed once a Thread becomes available.  If the Runnable has
   * already been removed from the queue this method silently ignores the promotion
   * request.
   *
   * @param runnable a Runnable
   */
  void promote(Runnable runnable);

  /**
   * Cancel the given Runnable if execution has not started yet.  In effect this
   * removes the Runnable from the queue.  If the Runnable was not in the queue
   * this method silently ignores the cancellation request.
   *
   * @param runnable a Runnable
   */
  void cancel(Runnable runnable);
}
