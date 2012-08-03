package hs.mediasystem.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class ScaledImageView extends Region {
  private final ImageView imageView = new ImageView();
  private final StackPane effectRegion = new StackPane();

  private final BooleanProperty zoom = new SimpleBooleanProperty(false);  // true = scale to cover whole area when preserveRatio is true
  public BooleanProperty zoomProperty() { return zoom; }
  public final boolean isZoom() { return zoom.get(); }
  public final void setZoom(boolean zoom) { this.zoom.set(zoom); }

  private final ObjectProperty<Pos> alignment = new SimpleObjectProperty<>(Pos.TOP_LEFT);
  public ObjectProperty<Pos> alignmentProperty() { return alignment; }
  public final Pos getAlignment() { return this.alignment.get(); }
  public final void setAlignment(Pos pos) { this.alignment.set(pos); }

  public ScaledImageView(Node placeHolder) {
    getChildren().add(imageView);
    getChildren().add(effectRegion);

    getStyleClass().add("scaled-image-view");

    effectRegion.getStyleClass().add("image-view");

    if(placeHolder != null) {
      effectRegion.getChildren().add(placeHolder);
      placeHolder.getStyleClass().add("place-holder");
      placeHolder.visibleProperty().bind(imageView.imageProperty().isNull());
    }
  }

  public ScaledImageView() {
    this(null);
  }

  @Override
  protected void layoutChildren() {
    Insets insets = effectRegion.getInsets();

    double insetsWidth = insets.getLeft() + insets.getRight();
    double insetsHeight = insets.getTop() + insets.getBottom();

    imageView.setFitWidth(getWidth() - insetsWidth);
    imageView.setFitHeight(getHeight() - insetsHeight);

    Bounds bounds = imageView.getLayoutBounds();

    if(zoom.get() && preserveRatioProperty().get() && (bounds.getWidth() != imageView.getFitWidth() || bounds.getHeight() != imageView.getFitHeight())) {

      /*
       * Need to define a viewport to make sure the image fills the entire available area while still
       * preserving ratio.
       */

      Image image = imageView.getImage();

      if(image != null) {
        double horizontalRatio = imageView.getFitWidth() / image.getWidth();
        double verticalRatio = imageView.getFitHeight() / image.getHeight();

        if(horizontalRatio > verticalRatio) {
          double viewportWidth = image.getWidth();
          double viewportHeight = imageView.getFitHeight() / horizontalRatio;

          double yOffset = alignment.get().getVpos() == VPos.BOTTOM ? image.getHeight() - viewportHeight :
                           alignment.get().getVpos() == VPos.CENTER ? (image.getHeight() - viewportHeight) / 2 : 0;

          imageView.setViewport(new Rectangle2D(0, yOffset, viewportWidth, viewportHeight));
        }
        else {
          double viewportWidth = imageView.getFitWidth() / verticalRatio;
          double viewportHeight = image.getHeight();

          double xOffset = alignment.get().getHpos() == HPos.RIGHT ? image.getWidth() - viewportWidth :
                           alignment.get().getHpos() == HPos.CENTER ? (image.getWidth() - viewportWidth) / 2 : 0;

          imageView.setViewport(new Rectangle2D(xOffset, 0, viewportWidth, viewportHeight));
        }
      }

      bounds = imageView.getLayoutBounds();
    }

    layoutInArea(imageView, insets.getLeft(), insets.getTop(), getWidth() - insetsWidth, getHeight() - insetsHeight, 0, alignment.get().getHpos(), alignment.get().getVpos());

    effectRegion.setMinWidth(Math.round(bounds.getWidth()) + insetsWidth);
    effectRegion.setMinHeight(Math.round(bounds.getHeight()) + insetsHeight);
    effectRegion.setMaxWidth(effectRegion.getMinWidth());
    effectRegion.setMaxHeight(effectRegion.getMinHeight());

    layoutInArea(effectRegion, 0, 0, getWidth(), getHeight(), 0, alignment.get().getHpos(), alignment.get().getVpos());
  }

  @Override
  protected double computePrefWidth(double height) {
    return 0;
  }

  @Override
  protected double computePrefHeight(double width) {
    return 0;
  }

  public final ObjectProperty<Image> imageProperty() { return imageView.imageProperty(); }
  public final Image getImage() { return imageView.getImage(); }
  public final void setImage(Image image) { imageView.setImage(image); }

  public final BooleanProperty preserveRatioProperty() { return imageView.preserveRatioProperty(); }
  public final boolean isPreserveRatio() { return imageView.isPreserveRatio(); }
  public final void setPreserveRatio(boolean preserveRatio) { imageView.setPreserveRatio(preserveRatio); }

  public final BooleanProperty smoothProperty() { return imageView.smoothProperty(); }
  public final boolean isSmooth() { return imageView.isSmooth(); }
  public final void setSmooth(boolean smooth) { imageView.setSmooth(smooth); }
}
