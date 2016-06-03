package hs.mediasystem.framework.actions;

import java.lang.reflect.InvocationTargetException;

import javafx.event.Event;

public class ExposedMethod implements ExposedMember {
  private final Member member;

  public ExposedMethod(Member member) {
    this.member = member;
  }

  @Override
  public Member getMember() {
    return member;
  }

  @Override
  public void doAction(String action, Object parent, Event event) {
    if(action.equals("trigger")) {
      try {
        getMember().getMethod().invoke(parent, event);
      }
      catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new IllegalStateException("action=" + action + ", member=" + member + ", object=" + parent + ", event=" + event, e);
      }
    }
    else {
      throw new IllegalArgumentException("Unknown action '" + action + "' for: " + getMember());
    }
  }
}
