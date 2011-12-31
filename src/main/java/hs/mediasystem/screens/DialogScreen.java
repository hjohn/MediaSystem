package hs.mediasystem.screens;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DialogScreen extends BorderPane {
//  private final List<Option> options;
  private final ObservableList<Node> options;
  private final List<List<Node>> optionStack  = new ArrayList<>();
  
  private int selectedIndex = 0;
  
  public DialogScreen(final String title, final List<Option> options) {
    final VBox optionList = new VBox() {{
      setId("dialog-list");

      for(Option option : options) {
        getChildren().add(option);
      }
      
      addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
        final KeyCombination tab = new KeyCodeCombination(KeyCode.TAB);
        final KeyCombination shiftTab = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
        final KeyCombination down = new KeyCodeCombination(KeyCode.DOWN);
        final KeyCombination up = new KeyCodeCombination(KeyCode.UP);
        final KeyCombination enter = new KeyCodeCombination(KeyCode.ENTER);
        final KeyCombination backspace = new KeyCodeCombination(KeyCode.BACK_SPACE);

        @Override
        public void handle(KeyEvent event) {
          Option selectedOption = options.get(selectedIndex);
          
          if(enter.match(event)) {
            if(selectedOption instanceof SubOption) {
              SubOption option = (SubOption)selectedOption;
              
              optionStack.add(new ArrayList<Node>(getChildren()));
              
              getChildren().clear();
              getChildren().addAll(option.getOptions());
              getChildren().get(0).requestFocus();
              selectedIndex = 0;
            }
            event.consume();
          }
          else if(event.getCode() == KeyCode.LEFT) {
            selectedOption.left();
            event.consume();
          }
          else if(event.getCode() == KeyCode.RIGHT) {
            selectedOption.right();
            event.consume();
          }
          else if(tab.match(event) || down.match(event)) {
            moveFocusNext();
            event.consume();
          }
          else if(shiftTab.match(event) || up.match(event)) {
            moveFocusPrevious();
            event.consume();
          }
          else if(backspace.match(event)) {
            if(!optionStack.isEmpty()) {
              getChildren().clear();
              getChildren().addAll(optionStack.remove(optionStack.size() - 1));
              getChildren().get(0).requestFocus();
              selectedIndex = 0;
              event.consume();
            }
          }
        }
      });
    }};
    
    this.options = optionList.getChildren();
    
    VBox box = new VBox() {{
      setId("dialog-main");
      setMaxSize(800, 600);
      
      getChildren().add(new Label(title) {{
        getStyleClass().add("title");
        setMaxWidth(Integer.MAX_VALUE);
      }});
      
      getChildren().add(optionList);
    }};
    
    setId("dialog");
    setCenter(box);
  }
  
  private void moveFocusNext() {
    int index = selectedIndex + 1;
    
    if(index >= options.size()) {
      index = 0;
    }

    options.get(index).requestFocus();
    selectedIndex = index;
  }
  
  private void moveFocusPrevious() {
    int index = selectedIndex - 1;
    
    if(index < 0) {
      index = options.size() - 1;
    }

    options.get(index).requestFocus();
    selectedIndex = index;
  }
}
