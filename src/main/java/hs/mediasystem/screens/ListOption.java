package hs.mediasystem.screens;

import hs.mediasystem.StringConverter;

import java.util.List;

import javafx.beans.property.ObjectProperty;

public class ListOption<T> extends Option {
  private final ObjectProperty<T> property;
  private final List<T> items;
  private final StringConverter<T> stringConverter;

  private T value;
  
  public ListOption(String description, ObjectProperty<T> property, List<T> items, StringConverter<T> stringConverter) {
    super(description);
    this.property = property;
    this.items = items;
    this.stringConverter = stringConverter;
    this.value = property.get();
    
    updateControl();
  }
  
  @Override
  public void left() {
    int index = items.indexOf(value) - 1;
    
    if(index < 0) {
      index = items.size() - 1;
    }
    
    value = items.get(index);
    
    updateControl();
  }
  
  @Override
  public void right() {
    int index = items.indexOf(value) + 1;
    
    if(index >= items.size()) {
      index = 0;
    }
    
    value = items.get(index);
    
    updateControl();
  }
  
  private void updateControl() {
    if(property != null) {
      property.set(value);
    }
    label.setText(stringConverter.toString(value));
  }
}