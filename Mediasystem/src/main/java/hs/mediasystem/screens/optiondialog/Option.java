package hs.mediasystem.screens.optiondialog;

import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class Option extends BorderPane {
  private final String description;

  protected final Label label = new Label();
  protected final Label bottomLabel = new Label("");

  public Option(String description) {
    this.description = description;

    setFocusTraversable(true);
    getStyleClass().add("option-cell");

    setLeft(new Label(description));
    setRight(label);
    setBottom(new VBox() {{
      getStyleClass().add("detail");
      getChildren().add(bottomLabel);
    }});
    bottomLabel.managedProperty().bind(bottomLabel.textProperty().isNotEqualTo(""));
  }

  public Label getBottomLabel() {
    return bottomLabel;
  }

  public String getDescription() {
    return description;
  }

  public void left() {
  }

  public void right() {
  }

  /**
   * @param event the event that triggered the select
   */
  public boolean select(Event event) {
    return false;
  }
}