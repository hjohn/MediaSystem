package hs.mediasystem.screens;

public class Navigator {
//  private final List<Destination> stack = new ArrayList<>();

  private Destination parent;
  private Destination current;
//  private int currentStackIndex = -1;

  public void back() {
    if(current != null) {
      if(current.previous != null) {
        current.outro();

        current = current.previous;

        current.intro();
        current.go();
      }
      else if(current.parent != null) {
        current.outro();
        current = current.parent;
        back();
      }
    }
  }

  public void navigateTo(Destination destination) {
    if(current != null) {
      if(parent == null) {
        current.outro();
      }

      current.next = destination;
    }

    destination.parent = parent;
    destination.previous = parent == null ? current : null;

    parent = destination;
    current = destination;

    destination.intro();
    destination.go();

    parent = null;
  }
}
