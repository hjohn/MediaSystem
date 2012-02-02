package hs.mediasystem.screens;

public class Navigator {
  private Destination parent;
  private Destination current;

  public void back() {
    System.out.println("[INFO] Navigator.back() - From: " + current);

    if(current != null) {
      if(current.previous != null && current.previous.parent != current.parent && current.parent != null) { // nested back
        current.outro();
        current = current.parent;
        back();
      }
      else if(current.previous != null) {
        current.outro();

        if(current.modal) {
          current = current.previous;
        }
        else {
          current = current.previous;

          callGo(current.parent);

          current.intro();
          current.go();
        }
      }
    }
  }

  private void callGo(Destination d) {
    if(d != null) {
      if(d.parent != null) {
        callGo(d.parent);
      }
      d.go();
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

  public void navigateParentTo(Destination destination) {
    System.out.println("[INFO] Navigator.navigateParentTo() - " + destination);
    System.out.println(">>> Navigating to : " + destination.getDescription());
    destination.modal = false;

    if(!destination.modal) {
      if(current != null) {
        if(parent == null) {
          current.outro();
        }

        //current.next = destination;
      }
    }

    if(parent != null || current == null || current.parent == null) {  // nested
      throw new IllegalStateException();
    }
    else {
      destination.parent = current.parent.parent;
      destination.previous = current;
    }

    parent = destination;
    current = destination;

    destination.intro();
    destination.go();

    parent = null;
  }

  private void navigate(Destination destination) {
    if(!destination.modal) {
      if(current != null) {
        if(parent == null) {
          current.outro();
        }

        //current.next = destination;
      }
    }

    if(parent != null) {  // nested
      destination.parent = parent;
      destination.previous = parent.previous;
    }
    else {
      destination.parent = current != null ? current.parent : null;
      destination.previous = current;
    }

    parent = destination;
    current = destination;

    destination.intro();
    destination.go();

    parent = null;
  }

  public static abstract class Destination {
    private final String description;

    private Destination previous;
    //private Destination next;
    private Destination parent;
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

    public Destination getParent() {
      return parent;
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
