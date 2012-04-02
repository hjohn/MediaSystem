package hs.mediasystem.beans;

import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.scene.image.Image;

public class AsyncImageProperty extends SimpleObjectProperty<Image> {
  private final ImageLoadService imageLoadService = new ImageLoadService();

  public AsyncImageProperty(final ObservableValue<ImageHandle> imageHandle) {
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
        synchronized(imageLoadService) {
          loadImageInBackground(imageHandle.getValue());
        }
      }
    });

    loadImageInBackground(imageHandle.getValue());
  }

  private void loadImageInBackground(ImageHandle imageHandle) {
    if(imageHandle != null) {
      imageLoadService.setImageHandle(imageHandle);
      imageLoadService.restart();
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
