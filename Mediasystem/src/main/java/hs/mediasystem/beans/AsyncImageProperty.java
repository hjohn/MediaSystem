package hs.mediasystem.beans;

import hs.mediasystem.util.AutoReentrantLock;
import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;
import hs.subtitle.DefaultThreadFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;

/**
 * Image property that loads the given ImageHandle in the background.<p>
 *
 * - When ImageHandle changes, Image is set to null and stays null for
 *   the settling duration plus the time to load a new Image.
 * - The background loading process will never set Image to a value that
 *   does not correspond to the current ImageHandle (when for example it
 *   was changed again before the loading completed).
 */
public class AsyncImageProperty extends SimpleObjectProperty<Image> {
  private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
  private static final ThreadPoolExecutor SLOW_EXECUTOR = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("AsyncImageProperty[Slow]", true));
  private static final ThreadPoolExecutor FAST_EXECUTOR = new ThreadPoolExecutor(3, 3, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("AsyncImageProperty[Fast]", true));

  private static Executor JAVAFX_UPDATE_EXECUTOR = new Executor() {
    @Override
    public void execute(Runnable command) {
      Platform.runLater(command);
    }
  };

  static {
    SLOW_EXECUTOR.allowCoreThreadTimeOut(true);
    FAST_EXECUTOR.allowCoreThreadTimeOut(true);
  }

  private final ObjectProperty<ImageHandle> imageHandle = new SimpleObjectProperty<>();
  public ObjectProperty<ImageHandle> imageHandleProperty() { return imageHandle; }

  private final AutoReentrantLock backgroundLoadLock = new AutoReentrantLock();

  /**
   * Contains the ImageHandle to load next, or null if the settling time has not expired yet.
   */
  private volatile ImageHandle imageHandleToLoad;     // Must hold backgroundLoadLock to access
  private volatile boolean backgroundLoadInProgress;  // Must hold backgroundLoadLock to access

  private ScheduledFuture<?> futureTaskAfterSettling; // Must be on JavaFX thread to access

  public AsyncImageProperty(long settlingMillis) {
    imageHandle.addListener(new ChangeListener<ImageHandle>() {
      @Override
      public void changed(ObservableValue<? extends ImageHandle> observable, ImageHandle oldValue, ImageHandle value) {
        set(null);

        if(futureTaskAfterSettling != null) {
          futureTaskAfterSettling.cancel(true);
          futureTaskAfterSettling = null;
        }

        if(value != null) {
          futureTaskAfterSettling = SCHEDULED_EXECUTOR_SERVICE.schedule(
            () -> setNewImageToLoad(new WeakReference<>(AsyncImageProperty.this), value),
            settlingMillis,
            TimeUnit.MILLISECONDS
          );
        }
      }
    });
  }

  public AsyncImageProperty() {
    this(500);
  }

  private void loadImageInBackgroundIfNeeded() {
    try(AutoReentrantLock lock = backgroundLoadLock.lock()) {
      if(!backgroundLoadInProgress && imageHandleToLoad != null) {
        backgroundLoadInProgress = true;
        loadImageInBackground(new WeakReference<>(this), imageHandleToLoad);
        imageHandleToLoad = null;
      }
    }
  }

  /**
   * Sets a new image to load (typically called after the settling delay expired).<p>
   *
   * Declared static so all references to instances must be by weak reference.
   *
   * @param propertyRef a weak reference to an instance of this class
   * @param imageHandle the image to load
   */
  private static void setNewImageToLoad(WeakReference<AsyncImageProperty> propertyRef, ImageHandle imageHandle) {
    AsyncImageProperty property = propertyRef.get();

    if(property != null) {
      try(AutoReentrantLock lock = property.backgroundLoadLock.lock()) {
        property.imageHandleToLoad = imageHandle;
        property.loadImageInBackgroundIfNeeded();
      }
    }
  }

  /**
   * Triggers the process that loads an image in the background.<p>
   *
   * Declared static so all references to instances must be by weak reference.
   *
   * @param propertyRef a weak reference to an instance of this class
   * @param imageHandle the image to load
   */
  private static void loadImageInBackground(WeakReference<AsyncImageProperty> propertyRef, ImageHandle imageHandle) {
    CompletableFuture
      .supplyAsync(() -> imageHandle.isFastSource() ? FAST_EXECUTOR : SLOW_EXECUTOR, FAST_EXECUTOR)
      .thenCompose(executor -> CompletableFuture.supplyAsync(() -> getImage(propertyRef, imageHandle), executor))
      .whenCompleteAsync((image, e) -> {
        AsyncImageProperty property = propertyRef.get();

        if(property != null) {
          try {
            if(e == null && imageHandle.equals(property.imageHandle.get())) {
              property.set(image);
            }
          }
          finally {
            try(AutoReentrantLock lock = property.backgroundLoadLock.lock()) {
              property.backgroundLoadInProgress = false;
              property.loadImageInBackgroundIfNeeded();
            }
          }
        }

        if(e != null && !(e.getCause() instanceof CancellationException)) {
          System.out.println("[WARN] AsyncImageProperty - Exception while loading " + imageHandle + " in background: " + e);
        }
      }, JAVAFX_UPDATE_EXECUTOR);
  }

  /**
   * Gets an Image from the Cache.<p>
   *
   * Declared static so all references to instances must be by weak reference.
   *
   * @param propertyRef a weak reference to an instance of this class
   * @param imageHandle the image to load
   */
  private static Image getImage(WeakReference<AsyncImageProperty> propertyRef, ImageHandle imageHandle) {
    final AsyncImageProperty asyncImagePropery = propertyRef.get();

    /*
     * Check if AsyncImageProperty still exists and its imageHandle hasn't changed:
     */

    if(asyncImagePropery != null && imageHandle.equals(asyncImagePropery.imageHandle.get())) {
      return ImageCache.loadImageUptoMaxSize(imageHandle, 1920, 1200);
    }

    throw new CancellationException();
  }
}
