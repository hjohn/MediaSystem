package hs.mediasystem.screens.optiondialog;

import hs.mediasystem.util.StringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class ListViewOption<T> extends Option {
  private static final KeyCombination UP = new KeyCodeCombination(KeyCode.UP);
  private static final KeyCombination DOWN = new KeyCodeCombination(KeyCode.DOWN);
  private static final KeyCombination LEFT = new KeyCodeCombination(KeyCode.LEFT);
  private static final KeyCombination RIGHT = new KeyCodeCombination(KeyCode.RIGHT);

  protected final ListView<T> listView = new ListView<>();
  protected boolean backOnSelect;

  private final ObjectProperty<T> property;

  public ListViewOption(final String description, final ObjectProperty<T> property, ObservableList<T> items, final StringConverter<T> stringConverter) {
    super(description);

    this.property = property;

    getStyleClass().clear();
    getStyleClass().add("list");

    setLeft(null);
    setRight(null);
    setTop(null);
    setCenter(new VBox() {{
      getChildren().add(new VBox() {{
        getStyleClass().add("header");
        getChildren().add(new Label(description));
      }});
      getChildren().add(listView);
    }});

    if(items != null) {
      listView.setItems(items);
    }

    listView.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
      @Override
      public ListCell<T> call(ListView<T> param) {
        return new ListCell<T>() {
          @Override
          protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if(item != null) {
              setText(stringConverter.toString(item));
            }
          }
        };
      }
    });

    listView.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {  // WORKAROUND this should be in setOnKeyPressed, but ListView does its own thing with left/right
      @Override
      public void handle(KeyEvent event) {
        if(LEFT.match(event)) {
          Event.fireEvent(listView.getParent(), event);
          event.consume();
        }
        else if(RIGHT.match(event)) {
          Event.fireEvent(listView.getParent(), event);
          event.consume();
        }
      }
    });

    listView.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
          Event.fireEvent(listView.getParent(), event);
          event.consume();
        }
        else if(UP.match(event)) {
          if(listView.getFocusModel().getFocusedIndex() == 0) {
            Event.fireEvent(listView.getParent(), event);
          }
        }
        else if(DOWN.match(event)) {
          if(listView.getFocusModel().getFocusedIndex() == listView.getItems().size() - 1) {
            Event.fireEvent(listView.getParent(), event);
          }
        }
//        else if(LEFT.match(event)) {
//          left();
//          event.consume();
//        }
//        else if(RIGHT.match(event)) {
//          right();
//          event.consume();
//        }
      }
    });
  }

  protected ListViewOption(final String description, final ObjectProperty<T> property, final StringConverter<T> stringConverter) {
    this(description, property, null, stringConverter);
  }

  @Override
  public boolean select() {
    if(property != null) {
      property.set(listView.getFocusModel().getFocusedItem());
    }

    return backOnSelect;
  }

  @Override
  public void requestFocus() {
    listView.requestFocus();

    if(listView.getSelectionModel().getSelectedIndex() == -1 && !listView.getItems().isEmpty()) {
      listView.getSelectionModel().select(0);
    }
  }
}