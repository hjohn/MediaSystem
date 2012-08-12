package hs.mediasystem.util;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CollectionPane extends StackPane {
  private final Map<Node, Double> orderMap = new WeakHashMap<>();

  public void add(String where, double order, Node node) {
    try {
      Pane pane = (Pane)lookup("#" + where);

      ObservableList<Node> children = pane.getChildren();
      int insertPosition;

      for(insertPosition = 0; insertPosition < children.size(); insertPosition++) {
        Node child = children.get(insertPosition);

        if(orderMap.get(child) > order) {
          break;
        }
      }

      children.add(insertPosition, node);
      orderMap.put(node, order);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    CollectionPane pane = new CollectionPane();
    GridPane gridPane = GridPaneUtil.create(new double[] {1, 1, 1}, new double[] {1});

    pane.getChildren().add(gridPane);

    VBox primary = new VBox();
    primary.setId("primary");

    VBox secondary = new VBox();
    secondary.setId("secondary");

    VBox tertiary = new VBox();
    tertiary.setId("tertiary");

    gridPane.add(primary, 0, 0);
    gridPane.add(secondary, 1, 0);
    gridPane.add(tertiary, 2, 0);
  }
}
