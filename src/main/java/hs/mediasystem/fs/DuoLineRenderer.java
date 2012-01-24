package hs.mediasystem.fs;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class DuoLineRenderer implements CellProvider<MediaItem> {

  private final HBox group = new HBox();

  private final Label title = new Label() {{
    getStyleClass().add("title");
  }};

  private final Label subtitle = new Label() {{
    setId("selectItem-listCell-subtitle");
    getStyleClass().add("subtitle");
  }};

  private final Label releaseYear = new Label() {{
    getStyleClass().add("release-year");
  }};

  private final Label ratingText = new Label() {{
    getStyleClass().add("rating");
  }};

  private final DoubleProperty rating = new SimpleDoubleProperty();

  private Group collectionMarker = new Group() {{
    getChildren().add(new Path() {{
      getElements().addAll(
        new MoveTo(0, 0),
        new LineTo(0, 30),
        new LineTo(15, 15),
        new LineTo(0, 0)
      );
    }});
  }};

  private HBox stars = new HBox() {{
    double[] points = createStar(8, 4, 5);

    for(int i = 0; i < 5; i++) {
      getChildren().add(new Polygon(points) {{
        getStyleClass().add("stars");
      }});
    }
  }};

  private HBox disabledStars = new HBox() {{
    double[] points = createStar(8, 4, 5);

    for(int i = 0; i < 5; i++) {
      getChildren().add(new Polygon(points) {{
        getStyleClass().add("stars");
      }});
    }
  }};

  private static double[] createStar(double radius, double innerRadius, int numberOfPoints) {
    double[] points = new double[numberOfPoints * 4];
    double[] radii = {radius, innerRadius};
    int p = 0;

    for(int i = 0; i < numberOfPoints * 2; i++) {
      points[p++] = radii[i % 2] * -Math.sin(i * Math.PI / numberOfPoints);
      points[p++] = radii[i % 2] * -Math.cos(i * Math.PI / numberOfPoints);
    }

    return points;
  }

  public DuoLineRenderer() {
    final HBox ratingNode = new HBox() {{
      getChildren().add(new Group() {{
        getChildren().add(disabledStars);
        disabledStars.setDisable(true);
        getChildren().add(stars);
        stars.setClip(new Rectangle(0, 0, 0, 20) {{
          this.widthProperty().bind(disabledStars.widthProperty().multiply(rating).divide(10));
        }});
      }});
      getChildren().add(ratingText);
    }};

    group.getChildren().add(new VBox() {{
      getChildren().add(new HBox() {{
        getChildren().add(title);
        getChildren().add(ratingNode);
      }});
      getChildren().add(subtitle);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
    group.getChildren().add(new VBox() {{
      getChildren().add(releaseYear);
    }});
    group.getChildren().add(collectionMarker);
  }

  @Override
  public Node configureCell(MediaItem item) {
    if(item != null) {
      title.setText(item.getTitle());
      subtitle.setText(item.getSubtitle());
      releaseYear.setText(item.getReleaseYear() == null ? "" : "" + item.getReleaseYear());
      ratingText.setText("" + item.getRating());
      rating.set(item.getRating() == null ? 0.0 : item.getRating());
      collectionMarker.setVisible(item instanceof hs.mediasystem.framework.Group);
    }

    return group;
  }
}