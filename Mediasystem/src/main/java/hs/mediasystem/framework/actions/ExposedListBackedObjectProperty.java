package hs.mediasystem.framework.actions;

import java.lang.reflect.Field;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;

public class ExposedListBackedObjectProperty extends AbstractExposedProperty<ObjectProperty<Object>> {
  private final Field valuesField;

  public ExposedListBackedObjectProperty(Member member, Field valuesField) {
    super(member);

    this.valuesField = valuesField;
  }

  @Override
  public void doAction(String action, Object parent, ObjectProperty<Object> property, Event event) {
    if(action.equals("next")) {
      try {
        @SuppressWarnings("unchecked")
        ObservableList<Object> list = (ObservableList<Object>)valuesField.get(parent);
        int currentIndex = list.indexOf(property.get());

        currentIndex++;

        if(currentIndex >= list.size()) {
          currentIndex = 0;
        }

        property.set(list.get(currentIndex));
      }
      catch(IllegalAccessException e) {
        throw new IllegalStateException(e);
      }
    }
    else {
      throw new IllegalStateException("Unknown action '" + action + "' for: " + property);
    }
  }
}