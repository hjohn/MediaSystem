package hs.mediasystem.screens.optiondialog;

import hs.mediasystem.util.DialogPane;

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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class OptionDialogPane extends DialogPane {
  private static final KeyCombination TAB = new KeyCodeCombination(KeyCode.TAB);
  private static final KeyCombination SHIFT_TAB = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
  private static final KeyCombination DOWN = new KeyCodeCombination(KeyCode.DOWN);
  private static final KeyCombination UP = new KeyCodeCombination(KeyCode.UP);
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final ObservableList<Node> options;
  private final List<List<Node>> optionStack  = new ArrayList<>();
  private final VBox optionList = new VBox();

  private int selectedIndex = 0;

  public OptionDialogPane(final String title, final List<? extends Option> options) {
    getStylesheets().add("dialog/option-dialog.css");

    optionList.getStyleClass().add("dialog-list");

    for(Option option : options) {
      optionList.getChildren().add(option);
    }

    optionList.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        Option selectedOption = (Option)optionList.getChildren().get(selectedIndex);

        if(ENTER.match(event)) {
          if(selectedOption instanceof OptionGroup) {
            OptionGroup option = (OptionGroup)selectedOption;

            optionStack.add(new ArrayList<>(optionList.getChildren()));

            optionList.getChildren().clear();
            optionList.getChildren().addAll(option.getOptions());
            optionList.getChildren().get(0).requestFocus();
            selectedIndex = 0;
          }
          else {
            if(selectedOption.select()) {
              back();
            }
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
          back();
          event.consume();
        }
      }
    });

    this.options = optionList.getChildren();

    VBox box = new VBox() {{
      setPrefSize(800, 600);
      setMaxSize(800, 600);

      getChildren().add(new Label(title) {{
        getStyleClass().add("title");
        setMaxWidth(Integer.MAX_VALUE);
      }});

      getChildren().add(optionList);
    }};

    getChildren().add(box);
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

  private void back() {
    if(!optionStack.isEmpty()) {
      optionList.getChildren().clear();
      optionList.getChildren().addAll(optionStack.remove(optionStack.size() - 1));
      optionList.getChildren().get(0).requestFocus();
      selectedIndex = 0;
    }
    else {
      close();
    }
  }

  @Override
  public void requestFocus() {
    Pane pane = (Pane)lookup(".dialog-list");

    if(!pane.getChildren().isEmpty()) {
      pane.getChildren().get(0).requestFocus();
    }
  }
}
