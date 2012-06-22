package hs.mediasystem.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class ScaledImageView extends Region {
  private final ImageView imageView = new ImageView();
  private final StackPane effectRegion = new StackPane();

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

    layoutInArea(imageView, insets.getLeft(), insets.getTop(), getWidth() - insetsWidth, getHeight() - insetsHeight, 0, alignment.get().getHpos(), alignment.get().getVpos());
    Bounds bounds = imageView.getLayoutBounds();

    effectRegion.setMinWidth(Math.round(bounds.getWidth()) + insetsWidth);
    effectRegion.setMinHeight(Math.round(bounds.getHeight()) + insetsHeight);
    effectRegion.setMaxWidth(effectRegion.getMinWidth());
    effectRegion.setMaxHeight(effectRegion.getMinHeight());

    layoutInArea(effectRegion,  0, 0, getWidth(), getHeight(), 0, alignment.get().getHpos(), alignment.get().getVpos());
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
