package hs.mediasystem;

import javafx.scene.Node;

public class NavigationItem {

  private final Node node;
  private final String stage;

  public NavigationItem(Node node, String stage) {
    this.node = node;
    this.stage = stage;
  }
  
  public Node getNode() {
    return node;
  }
  
  public String getStage() {
    return stage;
  }

}
