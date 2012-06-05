package hs.mediasystem.framework;

import hs.mediasystem.enrich.Parameters.WrappedValue;

public class TaskTitle implements WrappedValue<String> {
  private final String title;

  public TaskTitle(String title) {
    this.title = title;
  }

  @Override
  public String get() {
    return title;
  }
}
