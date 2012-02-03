package hs.mediasystem.screens;

public class Navigator {
  private final Navigator parentNavigator;

  private Destination current;

  public Navigator(Navigator parent) {
    this.parentNavigator = parent;
  }

  public Navigator() {
    this(null);
  }

  public void back() {
    System.out.println("[INFO] Navigator.back() - From: " + current);

    if(current != null) {
      if(current.previous != null) {
        current.outro();

        if(current.modal) {
          current = current.previous;
        }
        else {
          current = current.previous;

          current.intro();
          current.go();
        }
      }
      else if(parentNavigator != null) {
        parentNavigator.back();
      }
    }
  }

  public void navigateTo(Destination destination) {
    System.out.println("[INFO] Navigator.navigateTo() - " + destination);
    destination.modal = false;
    navigate(destination);
  }

  public void navigateToModal(Destination destination) {
    System.out.println("[INFO] Navigator.navigateToModal() - " + destination);
    destination.modal = true;
    navigate(destination);
  }

  private void navigate(Destination destination) {
    if(!destination.modal) {
      if(current != null) {
        current.outro();
      }
    }

    destination.previous = current;
    current = destination;

    destination.intro();
    destination.go();
  }

  public static abstract class Destination {
    private final String description;

    private Destination previous;
    //private Destination next;
    private boolean modal;

    public Destination(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }

    public Destination getPrevious() {
      return previous;
    }

    protected abstract void go();

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
