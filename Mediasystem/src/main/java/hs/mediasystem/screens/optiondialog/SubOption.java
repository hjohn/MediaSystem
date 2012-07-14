package hs.mediasystem.screens.optiondialog;

import java.util.List;

import javax.inject.Provider;

public class SubOption extends Option {
  private final Provider<List<Option>> optionCreator;

  public SubOption(String description, Provider<List<Option>> optionCreator) {
    super(description);

    this.optionCreator = optionCreator;
  }

  public List<Option> getOptions() {
    return optionCreator.get();
  }
}