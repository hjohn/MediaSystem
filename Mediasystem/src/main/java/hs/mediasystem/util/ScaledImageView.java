package hs.mediasystem.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class ScaledImageView extends ScrollPane {
  private final BorderPane borderPane = new BorderPane();
  private final ImageView imageView = new ImageView();

  public ScaledImageView() {
    imageView.fitWidthProperty().bind(widthProperty());
    imageView.fitHeightProperty().bind(heightProperty());
    imageView.setPreserveRatio(true);

    borderPane.prefWidthProperty().bind(widthProperty());
    borderPane.prefHeightProperty().bind(heightProperty());
    borderPane.setCenter(imageView);

    setContent(borderPane);
    setHbarPolicy(ScrollBarPolicy.NEVER);
    setVbarPolicy(ScrollBarPolicy.NEVER);
  }

  public final void setAlignment(Pos pos) {
    BorderPane.setAlignment(imageView, pos);
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

  public final void setImage(Image arg0) {
    imageView.setImage(arg0);
  }

  public final void setPreserveRatio(boolean arg0) {
    imageView.setPreserveRatio(arg0);
  }

  public final void setSmooth(boolean arg0) {
    imageView.setSmooth(arg0);
  }

  public final BooleanProperty smoothProperty() {
    return imageView.smoothProperty();
  }
}
