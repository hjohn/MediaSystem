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
  private static final Executor slowExecutor = new ThreadPoolExecutor(0, 2, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
  private static final Executor fastExecutor = new ThreadPoolExecutor(0, 3, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

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
    synchronized(fastExecutor) {
      if(!taskQueued) {
        if(imageHandle != null) {
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
                }

                final Image finalImage = image;

                Platform.runLater(new Runnable() {
                  @Override
                  public void run() {
                    set(finalImage);

                    ImageHandle handle = AsyncImageProperty.this.imageHandle.get();

                    if(handle != null && !handle.equals(imageHandle)) {
                      loadImageInBackground(handle);
                    }
                    else if(handle == null) {
                      set(null);
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
        else {
          set(null);
        }
      }
    }
  }
}
