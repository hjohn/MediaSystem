package hs.mediasystem.screens;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DuoLineCell extends HBox {
  private final IntegerProperty collectionSize = new SimpleIntegerProperty();

  public StringProperty titleProperty() { return title.textProperty(); }
  public StringProperty subtitleProperty() { return subtitle.textProperty(); }
  public StringProperty extraInfoProperty() { return extraInfo.textProperty(); }
  public DoubleProperty ratingProperty() { return starRating.ratingProperty(); }
  public IntegerProperty collectionSizeProperty() { return collectionSize; }
  public BooleanProperty viewedProperty() { return viewedIndicator.visibleProperty(); }

  private final Label title = new Label() {{
    getStyleClass().add("title");
  }};

  private final Label subtitle = new Label() {{
    getStyleClass().add("subtitle");
  }};

  private final Label viewedIndicator = new Label("\u2713") {{
    getStyleClass().add("viewed-indicator");
  }};

  private final Label extraInfo = new Label() {{
    getStyleClass().add("text");
  }};

  private final StarRating starRating = new StarRating(7, 3, 5);

  private final FlowPane collection = new FlowPane() {{
    getStyleClass().add("collection-text");
    setAlignment(Pos.CENTER_RIGHT);
    getChildren().add(new Label() {{
      textProperty().bind(collectionSizeProperty().asString());
      getStyleClass().add("count");
    }});
    getChildren().add(new Label(" items"));
  }};

  private final VBox description = new VBox() {{
    getChildren().addAll(title, subtitle);
  }};

  private final VBox info = new VBox() {{
    getStyleClass().add("info-block");
    getChildren().addAll(starRating, extraInfo);
    setFillWidth(false);
  }};

  public DuoLineCell() {
    subtitle.managedProperty().bind(subtitle.textProperty().isNotEqualTo(""));
    extraInfo.managedProperty().bind(extraInfo.textProperty().isNotEqualTo(""));

    collection.managedProperty().bind(collectionSize.isNotEqualTo(0));
    collection.visibleProperty().bind(collectionSize.isNotEqualTo(0));

    info.managedProperty().bind(collectionSize.isEqualTo(0));
    info.visibleProperty().bind(collectionSize.isEqualTo(0));

    setFillHeight(false);
    setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(description, Priority.ALWAYS);

    getChildren().addAll(description, viewedIndicator, collection, info);
  }
}