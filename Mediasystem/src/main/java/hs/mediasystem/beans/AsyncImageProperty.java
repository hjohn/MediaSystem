package hs.mediasystem.beans;

import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;
import hs.subtitle.DefaultThreadFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;

public class AsyncImageProperty extends SimpleObjectProperty<Image> {
  private static final ThreadPoolExecutor SLOW_EXECUTOR = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("AsyncImageProperty[Slow]", true));
  private static final ThreadPoolExecutor FAST_EXECUTOR = new ThreadPoolExecutor(3, 3, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DefaultThreadFactory("AsyncImageProperty[Fast]", true));
  private static final long NANOS_PER_MS = 1000L * 1000L;

  static {
    SLOW_EXECUTOR.allowCoreThreadTimeOut(true);
    FAST_EXECUTOR.allowCoreThreadTimeOut(true);
  }

  private final ObjectProperty<ImageHandle> imageHandle = new SimpleObjectProperty<>();
  public ObjectProperty<ImageHandle> imageHandleProperty() { return imageHandle; }

  private final long settlingNanos;

  private boolean taskQueued;

  public AsyncImageProperty(long settlingMillis) {
    this.settlingNanos = settlingMillis * NANOS_PER_MS;

    imageHandle.addListener(new ChangeListener<ImageHandle>() {
      @Override
      public void changed(ObservableValue<? extends ImageHandle> observable, ImageHandle oldValue, ImageHandle value) {
        loadImageInBackground(imageHandle.getValue());
      }
    });
  }

  public AsyncImageProperty() {
    this(500);
  }

  private void loadImageInBackground(final ImageHandle imageHandle) {
    set(null);

    synchronized(FAST_EXECUTOR) {
      if(!taskQueued && imageHandle != null) {
        taskQueued = true;

        Executor chosenExecutor = imageHandle.isFastSource() ? FAST_EXECUTOR : SLOW_EXECUTOR;

        chosenExecutor.execute(new BackgroundLoader(this, imageHandle, System.nanoTime() + settlingNanos));
      }
    }
  }

  private static final class BackgroundLoader implements Runnable {
    private final ImageHandle imageHandle;
    private final WeakReference<AsyncImageProperty> asyncImagePropertyReference;
    private final long loadAfterNanos;

    BackgroundLoader(AsyncImageProperty asyncImageProperty, ImageHandle imageHandle, long loadAfterNanos) {
      this.loadAfterNanos = loadAfterNanos;
      this.asyncImagePropertyReference = new WeakReference<>(asyncImageProperty);
      this.imageHandle = imageHandle;
    }

    @Override
    public void run() {
      sleepUntil(loadAfterNanos);

      final AsyncImageProperty asyncImagePropery = asyncImagePropertyReference.get();

      if(asyncImagePropery == null) {
        return;
      }

      try {
        Image image = null;

        if(imageHandle.equals(asyncImagePropery.imageHandle.get())) {
          try {
            image = ImageCache.loadImageUptoMaxSize(imageHandle, 1920, 1200);
          }
          catch(Exception e) {
            System.out.println("[WARN] AsyncImageProperty - Exception while loading " + imageHandle + " in background: " + e);
            e.printStackTrace(System.out);
          }
        }

        final Image finalImage = image;

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            asyncImagePropery.set(finalImage);

            ImageHandle handle = asyncImagePropery.imageHandle.get();

            if(handle == null || !handle.equals(imageHandle)) {
              asyncImagePropery.loadImageInBackground(handle);
            }
          }
        });
      }
      finally {
        synchronized(FAST_EXECUTOR) {
          asyncImagePropery.taskQueued = false;
        }
      }
    }

    private static void sleepUntil(long nanos) {
      for(;;) {
        long nanosLeft = nanos - System.nanoTime();

        if(nanosLeft <= 0) {
          break;
        }

        try {
          Thread.sleep(nanosLeft / NANOS_PER_MS + 1);
        }
        catch(InterruptedException e) {
          // Ignore
        }
      }
    }
  }
}
