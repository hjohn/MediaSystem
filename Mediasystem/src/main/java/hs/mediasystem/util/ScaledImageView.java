package hs.mediasystem.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class ScaledImageView extends Region {
  private final ImageView imageView = new ImageView();

  private final ObjectProperty<Pos> alignment = new SimpleObjectProperty<>(Pos.TOP_LEFT);
  public ObjectProperty<Pos> alignmentProperty() { return alignment; }
  public final Pos getAlignment() { return this.alignment.get(); }
  public final void setAlignment(Pos pos) { this.alignment.set(pos); }

  public ScaledImageView() {
    getChildren().add(imageView);
  }

  @Override
  protected void layoutChildren() {
    imageView.setFitWidth(getWidth());
    imageView.setFitHeight(getHeight());
    layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, alignment.get().getHpos(), alignment.get().getVpos());
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
