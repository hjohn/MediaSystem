package hs.mediasystem.util;

/**
 * An object that executes submitted {@link Runnable} tasks and allows manipulation by their assigned keys.
 *
 * Generally, implementors of this interface will only have a limited number of threads available for executing
 * the submitted tasks while the rest is queued.  As long as tasks are queued they can be manipulated with
 * their key.
 */
public interface KeyedExecutor {

  /**
   * Submits a new Runnable for future execution.
   *
   * @param runnable a runnable
   * @return a unique key that identifies this Runnable
   */
  Object submit(final Runnable runnable);

  /**
   * Promotes the Runnable associated with the given key to the head of the queue.  This makes this Runnable
   * the first to be executed once a Thread becomes available.
   *
   * @param key a unique key that identifies a Runnable
   */
  void promote(Object key);

  /**
   * Cancel the Runnable associated with the given key if execution has not started yet.  In effect this
   * removes the Runnable from the queue.
   *
   * @param key a unique key that identifies a Runnable
   */
  void cancel(Object key);
}
