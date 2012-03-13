package hs.mediasystem.screens.optiondialog;

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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class DialogScreen extends BorderPane {
  private static final KeyCombination TAB = new KeyCodeCombination(KeyCode.TAB);
  private static final KeyCombination SHIFT_TAB = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
  private static final KeyCombination DOWN = new KeyCodeCombination(KeyCode.DOWN);
  private static final KeyCombination UP = new KeyCodeCombination(KeyCode.UP);
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

//  private final List<Option> options;
  private final ObservableList<Node> options;
  private final List<List<Node>> optionStack  = new ArrayList<>();

  private int selectedIndex = 0;

  public DialogScreen(final String title, final List<? extends Option> options) {
    final VBox optionList = new VBox() {{
      setId("dialog-list");

      for(Option option : options) {
        getChildren().add(option);
      }

      addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
          Option selectedOption = (Option)getChildren().get(selectedIndex);

          if(ENTER.match(event)) {
            if(selectedOption instanceof SubOption) {
              SubOption option = (SubOption)selectedOption;

              optionStack.add(new ArrayList<>(getChildren()));

              getChildren().clear();
              getChildren().addAll(option.getOptions());
              getChildren().get(0).requestFocus();
              selectedIndex = 0;
            }
            else {
              selectedOption.select();
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
          else if(TAB.match(event) || DOWN.match(event)) {
            moveFocusNext();
            event.consume();
          }
          else if(SHIFT_TAB.match(event) || UP.match(event)) {
            moveFocusPrevious();
            event.consume();
          }
          else if(BACK_SPACE.match(event)) {
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

  @Override
  public void requestFocus() {
    ((Pane)lookup("#dialog-list")).getChildren().get(0).requestFocus();
  }
}
