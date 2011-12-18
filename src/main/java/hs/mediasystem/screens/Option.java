package hs.mediasystem.screens;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
        
        
//          addEventHandler(EventType.ROOT, new EventHandler<Event>() {
//            @Override
//            public void handle(Event event) {
//              System.out.println("Received event : " + event);
//            }
//          });

        addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
          final KeyCombination tab = new KeyCodeCombination(KeyCode.TAB);
          final KeyCombination shiftTab = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
          final KeyCombination down = new KeyCodeCombination(KeyCode.DOWN);
          final KeyCombination up = new KeyCodeCombination(KeyCode.UP);

          @Override
          public void handle(KeyEvent event) {
            if(event.getCode() == KeyCode.LEFT) {
              System.out.println("left");
              left();
              event.consume();
            }
            else if(event.getCode() == KeyCode.RIGHT) {
              System.out.println("right");
              right();
              event.consume();
            }
            else if(tab.match(event) || down.match(event)) {
              moveFocusNext(borderPane);
            }
            else if(shiftTab.match(event) || up.match(event)) {
              moveFocusPrevious(borderPane);
            }
          }
        });
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
  
  public Node getRightControl() {
    return new Label("<Value>");
  }
  
  private static void moveFocusNext(BorderPane borderPane) {
    ObservableList<Node> parentChildren = borderPane.getParent().getChildrenUnmodifiable();
    int indexInParent = parentChildren.indexOf(borderPane);
    
    if(indexInParent + 1 >= parentChildren.size()) {
      parentChildren.get(0).requestFocus();
    }
    else {
      parentChildren.get(indexInParent + 1).requestFocus();
    }
  }
  
  private static void moveFocusPrevious(BorderPane borderPane) {
    ObservableList<Node> parentChildren = borderPane.getParent().getChildrenUnmodifiable();
    int indexInParent = parentChildren.indexOf(borderPane);
    
    if(indexInParent == 0) {
      parentChildren.get(parentChildren.size() - 1).requestFocus();
    }
    else {
      parentChildren.get(indexInParent - 1).requestFocus();
    }
  }
}