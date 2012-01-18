package hs.mediasystem.screens;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;

public class NavigationHistory<T> {
  private final List<T> stack = new ArrayList<>();

  private int currentStackIndex = -1;

  public T back() {
    if(currentStackIndex == 0) {
      return null;
    }

    currentStackIndex--;

    onAction.get().handle(new ActionEvent(this, Event.NULL_SOURCE_TARGET));

    return stack.get(currentStackIndex);
  }

  public void forward(T destination) {
    while(stack.size() - 1 > currentStackIndex) {
      stack.remove(stack.size() - 1);
    }

    stack.add(destination);
    currentStackIndex++;

    onAction.get().handle(new ActionEvent(this, Event.NULL_SOURCE_TARGET));
  }

  public List<T> getPath() {
    List<T> list = new ArrayList<>();

    for(int index = 0; index <= currentStackIndex; index++) {
      list.add(stack.get(index));
    }

    return list;
  }

  public T current() {
    return currentStackIndex >= 0 ? stack.get(currentStackIndex) : null;
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }

  private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();

  public EventHandler<ActionEvent> getOnAction() {
    return onAction.get();
  }

  public void setOnAction(EventHandler<ActionEvent> value) {
    onAction.set(value);
  }

  public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
    return onAction;
  }

  //onChangeProperty();
}
