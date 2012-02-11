package hs.mediasystem.screens;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class Navigator {
  private final Navigator parentNavigator;

  private Destination current;

  public Navigator(final Navigator parent) {
    this.parentNavigator = parent;
  }

  public Navigator() {
    this(null);
  }

  public synchronized List<Destination> getTrail() {
    List<Destination> trail = new ArrayList<>();
    Destination dest = current;

    while(dest != null) {
      trail.add(0, dest);
      dest = dest.previous;
    }

    if(current.childNavigator != null) {
      List<Destination> childTrail = current.childNavigator.getTrail();
      trail.addAll(childTrail.subList(1, childTrail.size()));
    }

    return trail;
  }

  public synchronized void back() {
    System.out.println("[INFO] Navigator.back() - From: " + current);

    if(current != null) {
      if(current.previous != null) {
        current.doOutro();

        if(current.modal) {
          current = current.previous;
        }
        else {
          current = current.previous;

          current.doIntro();
          current.doExecute();
        }

        fireActionEvent();
      }
      else if(parentNavigator != null) {
        parentNavigator.current.childNavigator.onNavigation.set(null);
        parentNavigator.current.childNavigator = null;
        parentNavigator.back();
      }
    }
  }

  public synchronized void navigateTo(Destination destination) {
    System.out.println("[INFO] Navigator.navigateTo() - " + destination);
    destination.modal = false;
    navigate(destination);
  }

  public synchronized void navigateToModal(Destination destination) {
    System.out.println("[INFO] Navigator.navigateToModal() - " + destination);
    destination.modal = true;
    navigate(destination);
  }

  private void navigate(Destination destination) {
    if(!destination.modal) {
      if(current != null) {
        current.doOutro();
      }
    }

    if(parentNavigator != null && current == null) {
      parentNavigator.current.childNavigator = this;
      onNavigation.set(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          parentNavigator.fireActionEvent();
        }
      });
    }

    destination.previous = current;
    current = destination;

    destination.doIntro();
    destination.doExecute();

    fireActionEvent();
  }

  private void fireActionEvent() {
    EventHandler<ActionEvent> eventHandler = onNavigation.get();
    if(eventHandler != null) {
      eventHandler.handle(new ActionEvent(this, null));
    }
  }

  private final ObjectProperty<EventHandler<ActionEvent>> onNavigation = new SimpleObjectProperty<>();
  public ObjectProperty<EventHandler<ActionEvent>> onNavigation() { return onNavigation; }

  public static abstract class Destination {
    private final String description;

    private Navigator childNavigator;

    private Destination previous;
    //private Destination next;
    private boolean modal;
    private boolean initialised;

    public Destination(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    public Destination getPrevious() {
      return previous;
    }

    private void doInit() {
      if(!initialised) {
        initialised = true;
        init();
      }
    }

    private void doIntro() {
      doInit();
      intro();
    }

    private void doExecute() {
      doInit();
      execute();
    }

    private void doOutro() {
      doInit();
      outro();
    }

    protected void init() {
    }

    protected abstract void execute();

    protected void intro() {
    }

    protected void outro() {
    }

    @Override
    public String toString() {
      return "Destination('" + description + "'; modal=" + modal + ")";
    }
  }
}
