package hs.mediasystem.framework.actions;

import javafx.event.Event;

public class DummyExposedProperty implements ExposedMember {
  private final Member member;

  public DummyExposedProperty(Member member) {
    this.member = member;
  }

  @Override
  public Member getMember() {
    return member;
  }

  @Override
  public void doAction(String action, Object parent, Event event) {
    throw new UnsupportedOperationException();
  }
}