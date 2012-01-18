package hs.mediasystem.screens;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class Option extends BorderPane {
  private final String description;

  protected final Label label = new Label();

  public Option(String description) {
    this.description = description;

    setFocusTraversable(true);
    getStyleClass().add("cell");

    setLeft(new Label(description));
    setRight(label);
  }

  public String getDescription() {
    return description;
  }

  public void left() {
  }

  public void right() {
  }

  public void select() {
  }
}