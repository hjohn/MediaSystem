package hs.mediasystem.fs;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
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
  private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
  private final HBox group = new HBox();

  private final Label title = new Label() {{
    getStyleClass().add("title");
  }};

  private final Label subtitle = new Label() {{
    getStyleClass().add("subtitle");
  }};

  private final Label releaseTime = new Label() {{
    getStyleClass().add("release-time");
    setAlignment(Pos.CENTER);
    setMaxWidth(1000);
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

  private static final double[] STAR_DATA = createStar(7, 3, 5);

  private HBox stars = new HBox() {{
    for(int i = 0; i < 5; i++) {
      getChildren().add(new Polygon(STAR_DATA) {{
        getStyleClass().add("star");
      }});
    }
  }};

  private HBox disabledStars = new HBox() {{
    for(int i = 0; i < 5; i++) {
      getChildren().add(new Polygon(STAR_DATA) {{
        getStyleClass().add("star");
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

  private final VBox description = new VBox() {{
    setAlignment(Pos.CENTER_LEFT);
  }};

  public DuoLineRenderer() {
    final HBox ratingNode = new HBox() {{
      getStyleClass().add("rating");

      getChildren().add(new Group() {{
        getChildren().add(disabledStars);
        disabledStars.setDisable(true);
        getChildren().add(stars);
      }});
//      getChildren().add(ratingText);
    }};

    group.getChildren().add(new HBox() {{
      getChildren().add(new VBox() {{
        getChildren().add(ratingNode);
        getChildren().add(releaseTime);
      }});

      getChildren().add(description);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
//    group.getChildren().add(new VBox() {{
//      // getChildren().add(ratingNode);
//     //  getChildren().add(releaseYear);
//    }});
    group.getChildren().add(collectionMarker);
  }

  @Override
  public Node configureCell(MediaItem item) {
    if(item != null) {
      title.setText(item.getTitle());
      subtitle.setText(item.getSubtitle());

      description.getChildren().clear();
      description.getChildren().add(title);
      if(!item.getSubtitle().isEmpty()) {
        description.getChildren().add(subtitle);
      }

      String releaseDate = item.getReleaseDate() == null ? null : (dateFormat.format(item.getReleaseDate()) + " ");
      if(releaseDate == null) {
        releaseDate = item.getReleaseYear() == null ? "" : ("" + item.getReleaseYear() + " ");
      }

      releaseTime.setText(releaseDate);
      ratingText.setText("" + item.getRating());
      rating.set(item.getRating() == null ? 0.0 : item.getRating());
      collectionMarker.setVisible(item instanceof hs.mediasystem.framework.Group);

      stars.setClip(new Rectangle(0, 0, 0, 20) {{
        this.widthProperty().bind(disabledStars.widthProperty().multiply(rating).divide(10));  // TODO one would expect this to update automatically, but it doesn't
      }});
    }

    return group;
  }
}