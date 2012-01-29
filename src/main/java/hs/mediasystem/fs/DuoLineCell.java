package hs.mediasystem.fs;

import hs.mediasystem.screens.StarRating;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class DuoLineCell extends HBox {
  private final Label title = new Label() {{
    getStyleClass().add("title");
  }};

  private final Label subtitle = new Label() {{
    getStyleClass().add("subtitle");
  }};

  private final Label extraInfo = new Label() {{
    getStyleClass().add("extra-info");
    setAlignment(Pos.CENTER);
    setMaxWidth(1000);
  }};

  private final StarRating starRating = new StarRating(7, 3, 5);

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

  public DuoLineCell() {
    description.getChildren().add(title);
    description.getChildren().add(subtitle);

    subtitle.managedProperty().bind(subtitle.textProperty().isNotEqualTo(""));
    extraInfo.managedProperty().bind(extraInfo.textProperty().isNotEqualTo(""));

    getChildren().add(new HBox() {{
      getChildren().add(new VBox() {{
        getChildren().add(starRating);
        getChildren().add(extraInfo);
      }});

      getChildren().add(description);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
    getChildren().add(collectionMarker);
  }

  public StringProperty titleProperty() { return title.textProperty(); }
  public StringProperty subtitleProperty() { return subtitle.textProperty(); }
  public StringProperty extraInfoProperty() { return extraInfo.textProperty(); }
  public DoubleProperty ratingProperty() { return starRating.ratingProperty(); }
  public BooleanProperty groupProperty() { return collectionMarker.visibleProperty(); }
}