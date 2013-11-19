package hs.mediasystem.controls;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

public class TablePane extends GridPane {
  private int row = 0;
  private int column = 0;

  public TablePane add(Node node) {
    getChildren().add(node);
    GridPane.setConstraints(node, column++, row);
    return this;
  }

  public TablePane nextRow() {
    column = 0;
    row++;
    return this;
  }
}
