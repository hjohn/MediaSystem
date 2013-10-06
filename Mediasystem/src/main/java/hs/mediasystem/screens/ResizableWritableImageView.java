package hs.mediasystem.screens;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ResizableWritableImageView extends ImageView {
  private final DoubleProperty width = new SimpleDoubleProperty();
  public final double getWidth() { return width.get(); }
  public final void setWidth(double width) { this.width.set(width); }
  public final DoubleProperty widthProperty() { return width; }

  private final DoubleProperty height = new SimpleDoubleProperty();
  public final double getHeight() { return height.get(); }
  public final void setHeight(double height) { this.height.set(height); }
  public final DoubleProperty heightProperty() { return height; }

  public ResizableWritableImageView(double w, double h) {
    width.addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        updateImageSize();
      }
    });

    height.addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        updateImageSize();
      }
    });

    width.set(w);
    height.set(h);
  }

  @Override
  public void resize(double w, double h) {
    setWidth(w);
    setHeight(h);
  }

  public PixelWriter getPixelWriter() {
    return ((WritableImage)getImage()).getPixelWriter();
  }

  private void updateImageSize() {
    int w = (int)getWidth();
    int h = (int)getHeight();

    if(w < 1) {
      w = 1;
    }
    if(h < 1) {
      h = 1;
    }

    setImage(new WritableImage(w, h));
  }
}
