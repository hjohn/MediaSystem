package hs.mediasystem.screens;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class StarRating extends HBox {
  private final DoubleProperty rating = new SimpleDoubleProperty(0.0);
  public DoubleProperty ratingProperty() { return rating; }

  private final Rectangle rectangle = new Rectangle(0, 0, 0, 0) {{  // TODO scale seems to work, but too much work to get it to work for now (with width at 100)
  }};

  public StarRating(final double radius, final double innerRadius, final int numberOfPoints) {
    getStyleClass().add("rating");

    double[] starData = createStarData(radius, innerRadius, numberOfPoints);

    final HBox stars = createStars(starData);
    final HBox disabledStars = createStars(starData);

    getChildren().add(new Group() {{
      disabledStars.setDisable(true);
      stars.setClip(rectangle);

      getChildren().addAll(disabledStars, stars);
    }});

    rating.addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number value) {
        // TODO for some reason, a direct bind on rating is simply not working for changing the width property of the clip.
        // rectangle.scaleXProperty().set(100.0 / disabledStars.getWidth() * rating.get());
        stars.setClip(new Rectangle(0, 0, disabledStars.getWidth() * rating.get(), disabledStars.getHeight()));
      }
    });
  }

  private static HBox createStars(final double[] points) {
    return new HBox() {{
      for(int i = 0; i < 5; i++) {
        getChildren().add(new Polygon(points) {{
          getStyleClass().add("star");
        }});
      }
    }};
  }

  private static double[] createStarData(double radius, double innerRadius, int numberOfPoints) {
    double[] points = new double[numberOfPoints * 4];
    double[] radii = {radius, innerRadius};
    int p = 0;

    for(int i = 0; i < numberOfPoints * 2; i++) {
      points[p++] = radii[i % 2] * -Math.sin(i * Math.PI / numberOfPoints);
      points[p++] = radii[i % 2] * -Math.cos(i * Math.PI / numberOfPoints);
    }

    return points;
  }
}
