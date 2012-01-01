package hs.mediasystem.screens;

import hs.mediasystem.StringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class ListViewOption<T> extends Option {
  private final ListView<T> listView = new ListView<>();
  
  public ListViewOption(String description, final ObjectProperty<T> property, ObservableList<T> items, final StringConverter<T> stringConverter) {
    super(description);
    
    setLeft(null);
    setRight(null);
    setTop(new Label(description));
    setCenter(listView);
    
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
    
    listView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
          if(property != null) {
            property.set(listView.getSelectionModel().getSelectedItem());
          }
        }
      }
    });
  }
  
  @Override
  public void requestFocus() {
    listView.requestFocus();
    listView.getFocusModel().focusNext();
  }
}