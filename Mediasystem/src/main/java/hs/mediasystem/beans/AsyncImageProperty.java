package hs.mediasystem.beans;

import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;

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
  private static final ThreadPoolExecutor slowExecutor = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
  private static final ThreadPoolExecutor fastExecutor = new ThreadPoolExecutor(3, 3, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

  static {
    slowExecutor.allowCoreThreadTimeOut(true);
    fastExecutor.allowCoreThreadTimeOut(true);
  }

  private final ObjectProperty<ImageHandle> imageHandle = new SimpleObjectProperty<>();

  private boolean taskQueued;

  public AsyncImageProperty() {
    imageHandle.addListener(new ChangeListener<ImageHandle>() {
      @Override
      public void changed(ObservableValue<? extends ImageHandle> observable, ImageHandle oldValue, ImageHandle value) {
        loadImageInBackground(imageHandle.getValue());
      }
    });
  }

  public ObjectProperty<ImageHandle> imageHandleProperty() {
    return imageHandle;
  }

  private void loadImageInBackground(final ImageHandle imageHandle) {
    set(null);

    synchronized(fastExecutor) {
      if(!taskQueued && imageHandle != null) {
        Executor chosenExecutor = imageHandle.isFastSource() ? fastExecutor : slowExecutor;

        chosenExecutor.execute(new Runnable() {
          @Override
          public void run() {
            try {
              Image image = null;

              try {
                image = ImageCache.loadImage(imageHandle);
              }
              catch(Exception e) {
                System.out.println("[WARN] AsyncImageProperty - Exception while loading " + imageHandle + " in background: " + e);
                e.printStackTrace(System.out);
              }

              final Image finalImage = image;

              Platform.runLater(new Runnable() {
                @Override
                public void run() {
                  set(finalImage);

                  ImageHandle handle = AsyncImageProperty.this.imageHandle.get();

                  if(handle == null || !handle.equals(imageHandle)) {
                    loadImageInBackground(handle);
                  }
                }
              });
            }
            finally {
              synchronized(fastExecutor) {
                taskQueued = false;
              }
            }
          }
        });

        taskQueued = true;
      }
    }
  }
}
