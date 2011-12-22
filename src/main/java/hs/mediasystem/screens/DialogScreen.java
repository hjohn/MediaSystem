package hs.mediasystem.screens;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DialogScreen extends BorderPane {
  private final ObservableList<Option> options;
  
  private int selectedIndex = 0;
  
  public DialogScreen(final String title, final ObservableList<Option> options) {
    this.options = options;

    VBox box = new VBox() {{
      setId("dialog");
      setMaxSize(800, 600);
      
      getChildren().add(new Label(title) {{
        getStyleClass().add("title");
        setMaxWidth(Integer.MAX_VALUE);
      }});
      
      getChildren().add(new VBox() {{
        for(Option option : options) {
          getChildren().add(option.getControl());
        }
        
        addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
          final KeyCombination tab = new KeyCodeCombination(KeyCode.TAB);
          final KeyCombination shiftTab = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
          final KeyCombination down = new KeyCodeCombination(KeyCode.DOWN);
          final KeyCombination up = new KeyCodeCombination(KeyCode.UP);
          final KeyCombination enter = new KeyCodeCombination(KeyCode.ENTER);

          @Override
          public void handle(KeyEvent event) {
            Option selectedOption = options.get(selectedIndex);
            
            if(enter.match(event)) {
              selectedOption.select();
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
          }
        });

      }});  
    }};
    
    setId("dialog-screen");
    setCenter(box);
  }
  
  private void moveFocusNext() {
    int index = selectedIndex + 1;
    
    if(index >= options.size()) {
      index = 0;
    }

    options.get(index).getControl().requestFocus();
    selectedIndex = index;
  }
  
  private void moveFocusPrevious() {
    int index = selectedIndex - 1;
    
    if(index < 0) {
      index = options.size() - 1;
    }

    options.get(index).getControl().requestFocus();
    selectedIndex = index;
  }
}
