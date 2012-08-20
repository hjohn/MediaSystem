package hs.mediasystem.util;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class AreaPane extends StackPane {
  private final Map<Node, Double> orderMap = new WeakHashMap<>();

  public boolean add(String where, double order, Node node) {
    Pane pane = (Pane)lookup("#" + where);

    if(pane == null) {
      return false;
    }

    ObservableList<Node> children = pane.getChildren();
    int insertPosition = 0;

    for(; insertPosition < children.size(); insertPosition++) {
      Node child = children.get(insertPosition);

      if(orderMap.get(child) > order) {
        break;
      }
    }

    children.add(insertPosition, node);
    orderMap.put(node, order);

    return true;
  }
}
