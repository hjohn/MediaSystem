package hs.mediasystem.framework.actions;

import javafx.beans.property.ObjectProperty;
import javafx.event.Event;

public class ExposedActionObjectProperty extends AbstractExposedProperty<ObjectProperty<Object>> {
  private final ValueBuilder<Object> valueBuilder;

  public ExposedActionObjectProperty(Member member, ValueBuilder<Object> valueBuilder) {
    super(member);

    this.valueBuilder = valueBuilder;
  }

  @Override
  public void doAction(String action, Object parent, ObjectProperty<Object> property, Event event) {
    if(action.equals("trigger")) {
      property.set(valueBuilder.build(event, property.get()));
    }
    else {
      throw new IllegalStateException("Unknown action '" + action + "' for: " + property);
    }
  }
}