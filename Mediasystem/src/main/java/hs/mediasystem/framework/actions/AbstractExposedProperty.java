package hs.mediasystem.framework.actions;

import javafx.beans.property.Property;
import javafx.event.Event;

public abstract class AbstractExposedProperty<T extends Property<?>> implements ExposedMember {
  private final Member member;

  public AbstractExposedProperty(Member member) {
    this.member = member;
  }

  @Override
  public Member getMember() {
    return member;
  }

  @SuppressWarnings("unchecked")
  @Override
  public final void doAction(String action, Object parent, Event event) {
    doAction(action, parent, (T)getMember().get(parent), event);
  }

  public abstract void doAction(String action, Object parent, T property, Event event);
}