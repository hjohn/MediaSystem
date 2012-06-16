package hs.mediasystem.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class ScaledImageView extends Region {
  private final ImageView imageView = new ImageView();
  private Pos alignment = Pos.TOP_LEFT;

  public ScaledImageView() {
    getChildren().add(imageView);
  }

  @Override
  protected void layoutChildren() {
    imageView.setFitWidth(getWidth());
    imageView.setFitHeight(getHeight());
    layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, alignment.getHpos(), alignment.getVpos());
  }

  public final void setAlignment(Pos pos) {
    this.alignment = pos;
    this.requestLayout();
  }

  public final Image getImage() {
    return imageView.getImage();
  }

  public final ObjectProperty<Image> imageProperty() {
    return imageView.imageProperty();
  }

  public final boolean isPreserveRatio() {
    return imageView.isPreserveRatio();
  }

  public final boolean isSmooth() {
    return imageView.isSmooth();
  }

  public final BooleanProperty preserveRatioProperty() {
    return imageView.preserveRatioProperty();
  }

  public final void setImage(Image image) {
    imageView.setImage(image);
  }

  public final void setPreserveRatio(boolean preserveRatio) {
    imageView.setPreserveRatio(preserveRatio);
  }

  public final void setSmooth(boolean preserveRatio) {
    imageView.setSmooth(preserveRatio);
  }

  public final BooleanProperty smoothProperty() {
    return imageView.smoothProperty();
  }
}
