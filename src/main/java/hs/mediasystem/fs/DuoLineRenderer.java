package hs.mediasystem.fs;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.StarRating;
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

public class DuoLineRenderer implements CellProvider<MediaItem> {
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

  private final StarRating starRating = new StarRating(7, 3, 5);

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

  private final VBox description = new VBox() {{
    setAlignment(Pos.CENTER_LEFT);
  }};

  public DuoLineRenderer() {
    starRating.ratingProperty().bind(rating.divide(10));

    group.getChildren().add(new HBox() {{
      getChildren().add(new VBox() {{
        getChildren().add(starRating);
        getChildren().add(releaseTime);
      }});

      getChildren().add(description);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
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

      releaseTime.setText(MediaItemFormatter.formatReleaseTime(item));
      ratingText.setText("" + item.getRating());
      rating.set(item.getRating() == null ? 0.0 : item.getRating());
      collectionMarker.setVisible(item instanceof hs.mediasystem.framework.Group);
    }

    return group;
  }
}