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

  private final ListView<T> listView = new ListView<>();

  public ListViewOption(final String description, final ObjectProperty<T> property, ObservableList<T> items, final StringConverter<T> stringConverter) {
    super(description);

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

    listView.setItems(items);
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

    listView.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
          if(property != null) {
            property.set(listView.getSelectionModel().getSelectedItem());
          }
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
      }
    });
  }

  @Override
  public void requestFocus() {
    listView.requestFocus();
    if(listView.getFocusModel().getFocusedIndex() == -1) {
      listView.getFocusModel().focusNext();
    }
  }
}