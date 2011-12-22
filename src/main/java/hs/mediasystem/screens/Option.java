package hs.mediasystem.screens;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class Option {
  private final String description;
  private BorderPane borderPane;

  public Option(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }

  public Node getControl() {
    if(borderPane == null) {
      borderPane = new BorderPane() {{
        setFocusTraversable(true);
        getStyleClass().add("cell");
        
        setLeft(new Label(getDescription()));
        setRight(getRightControl());
      }};
    }
    
    return borderPane;
  }
  
  public void left() {
  }
  
  public void right() {
  }
  
  public void select() {
  }
  
  public Node getRightControl() {
    return new Label("<Value>");
  }
}