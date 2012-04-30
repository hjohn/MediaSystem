package hs.mediasystem.beans;

import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.scene.image.Image;

public class AsyncImageProperty extends SimpleObjectProperty<Image> {
  private final ImageLoadService imageLoadService = new ImageLoadService();
  private final ObjectProperty<ImageHandle> imageHandle = new SimpleObjectProperty<>();

  public AsyncImageProperty() {
    imageLoadService.stateProperty().addListener(new ChangeListener<State>() {
      @Override
      public void changed(ObservableValue<? extends State> observable, State oldValue, State value) {
        if(value == State.SUCCEEDED) {
          set(imageLoadService.getValue());
        }
        if(value == State.FAILED) {
          set(null);
        }
      }
    });

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

  private void loadImageInBackground(ImageHandle imageHandle) {
    synchronized(imageLoadService) {
      if(imageHandle != null) {
        imageLoadService.setImageHandle(imageHandle);
        try {
          imageLoadService.restart();
        }
        catch(IllegalStateException e) {  // WORKAROUND: If this happens, I donot want the whole chain of events to break down, so this exception is swallowed here
          System.out.println("[FINE] AsyncImageProperty.loadImageInBackground() - WORKAROUND, swallowing IllegalStateException for now");
        }
      }
    }
  }

  private static class ImageLoadService extends Service<Image> {
    private ImageHandle imageHandle;

    public void setImageHandle(ImageHandle imageHandle) {
      this.imageHandle = imageHandle;
    }

    @Override
    protected Task<Image> createTask() {
      final ImageHandle imageHandle = this.imageHandle;

      return new Task<Image>() {
        @Override
        protected Image call() {
          return ImageCache.loadImage(imageHandle);
        }
      };
    }
  }
}
