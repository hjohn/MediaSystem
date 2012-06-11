package hs.mediasystem.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.StackPane;

public class RangeBar extends StackPane {

  private final DoubleProperty min = new SimpleDoubleProperty(0.0);
  private final DoubleProperty max = new SimpleDoubleProperty(1.0);

  private final DoubleProperty origin = new SimpleDoubleProperty(0.0);
  public DoubleProperty originProperty() { return origin; }

  private final DoubleProperty value = new SimpleDoubleProperty(0.0);
  public DoubleProperty valueProperty() { return value; }

  private final StackPane track = new StackPane();
  private final StackPane range = new StackPane();
  private final StackPane thumb = new StackPane();

  public RangeBar() {
    getStylesheets().add("range-bar.css");

    getStyleClass().add("range-bar");

    track.getStyleClass().add("track");
    range.getStyleClass().add("range");
    thumb.getStyleClass().add("thumb");

    getChildren().addAll(track, range, thumb);
  }

  @Override
  protected double computeMinHeight(double width) {
    return computeMaxHeight(width);
  }

  @Override
  protected double computeMaxHeight(double width) {
    return track.prefHeight(width) + getInsets().getTop() + getInsets().getBottom();
  }

  @Override
  protected void layoutChildren() {
    Insets insets = getInsets();

    double left = insets.getLeft();
    double top = insets.getTop();
    double trackWidth = getWidth() - getInsets().getLeft() - getInsets().getRight();
    double height = getHeight() - getInsets().getTop() - getInsets().getBottom();

    layoutInArea(track, left, top, trackWidth, height, 0, null, true, true, HPos.CENTER, VPos.CENTER);

    double rangeWidth = trackWidth / (max.get() - min.get()) * Math.abs(origin.get() - value.get());
    double rangeStart = trackWidth / (max.get() - min.get()) * Math.min(origin.get(), value.get());

    layoutInArea(range, left + rangeStart, top, rangeWidth, height, 0, null, true, true, HPos.CENTER, VPos.CENTER);

    double thumbWidth = track.getHeight();
    double thumbPosition = trackWidth / (max.get() - min.get()) * value.get() - thumbWidth / 2;

    layoutInArea(thumb, left + thumbPosition, insets.getTop(), thumbWidth, thumbWidth, 0, null, true, true, HPos.CENTER, VPos.CENTER);
  }
}
