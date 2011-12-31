package hs.mediasystem.screens;

import hs.mediasystem.StringConverter;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ListViewOption<T> extends Option {
  private final ObjectProperty<T> property;

  private final ListView<T> listView = new ListView<>();
  
  private T value;
  
  public ListViewOption(String description, ObjectProperty<T> property, ObservableList<T> items, final StringConverter<T> stringConverter) {
    super(description);
    this.property = property;
    
    if(property != null) {
      this.value = property.get();
    }
    
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
    
    updateControl();
  }
  
  private void updateControl() {
    if(property != null) {
      property.set(value);
    }
  }
  
  @Override
  public void requestFocus() {
    listView.requestFocus();
    listView.getFocusModel().focusNext();
  }
}