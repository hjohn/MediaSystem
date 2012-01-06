package hs.mediasystem.screens;

import hs.mediasystem.Callable;

import java.util.List;

public class SubOption extends Option {
  private final Callable<List<Option>> optionCreator;

  public SubOption(String description, Callable<List<Option>> optionCreator) {
    super(description);

    this.optionCreator = optionCreator;
  }

  public List<Option> getOptions() {
    return optionCreator.call();
  }
}