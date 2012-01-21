package hs.mediasystem.screens;

import hs.mediasystem.util.Callable;

public class ActionOption extends Option {
  private final Callable<Boolean> callable;

  public ActionOption(String description, Callable<Boolean> callable) {
    super(description);
    this.callable = callable;
  }

  @Override
  public void select() {
    callable.call();
  }
}
