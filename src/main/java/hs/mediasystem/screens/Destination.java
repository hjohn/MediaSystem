package hs.mediasystem.screens;

public class Destination {
  private final String description;
  private final Runnable runnable;

  Destination previous;
  Destination next;
  Destination parent;

  public Destination(String description, Runnable runnable) {
    this.description = description;
    this.runnable = runnable;
  }

  public String getDescription() {
    return description;
  }

  public void go() {
    runnable.run();
  }

  public void intro() {
  }

  public void outro() {
  }
}
