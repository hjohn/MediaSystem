package hs.mediasystem.screens.optiondialog;

import hs.mediasystem.util.Callable;

public class ActionOption extends Option {
  private final Callable<Void> callable;

  public ActionOption(String description, Callable<Void> callable) {
    super(description);
    this.callable = callable;
  }

  @Override
  public void select() {
    callable.call();
  }
}
