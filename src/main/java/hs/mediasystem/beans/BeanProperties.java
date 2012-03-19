package hs.mediasystem.beans;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;

public class BeanProperties {
  private static final Map<Updatable, TimedUpdatable> timedUpdatables = new WeakHashMap<>();

  static {
    new Thread() {
      {
        setName("BeanProperties poller");
        setDaemon(true);
      }

      @Override
      public void run() {
        try {
          for(;;) {
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                for(Updatable updatable : timedUpdatables.keySet()) {
                  if(updatable != null && timedUpdatables.get(updatable).shouldUpdate()) {
                    updatable.update();
                  }
                }
              }
            });

            Thread.sleep(100);
          }
        }
        catch(InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }.start();
  }

  public static class TimedUpdatable {
    private final long pollMillis;

    private long lastPollTime;

    public TimedUpdatable(long pollMillis) {
      this.pollMillis = pollMillis;
    }

    public boolean shouldUpdate() {
      long currentTimeMillis = System.currentTimeMillis();

      if(lastPollTime + pollMillis < currentTimeMillis) {
        lastPollTime = currentTimeMillis;
        return true;
      }

      return false;
    }
  }

  public static LongProperty createLongProperty(String writeMethodName, Object instance, String... propertyNames) {
    return new BeanLongProperty(new BeanAccessor<Long>(writeMethodName, instance, propertyNames));
  }

  public static LongProperty createLongProperty(Object instance, String... propertyNames) {
    return createLongProperty(null, instance, propertyNames);
  }

  public static IntegerProperty createIntegerProperty(String writeMethodName, Object instance, String... propertyNames) {
    return new BeanIntegerProperty(new BeanAccessor<Integer>(writeMethodName, instance, propertyNames));
  }

  public static IntegerProperty createIntegerProperty(Object instance, String... propertyNames) {
    return createIntegerProperty(null, instance, propertyNames);
  }

  public static void pollProperty(Updatable property, long pollMillis) {
    timedUpdatables.put(property, new TimedUpdatable(pollMillis));
  }

  public static void stopPollProperty(Updatable property) {
    timedUpdatables.remove(property);
  }
}
