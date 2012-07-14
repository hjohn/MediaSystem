package hs.mediasystem.screens.optiondialog;

import java.util.List;

import javax.inject.Provider;

public class OptionGroup extends Option {
  private final Provider<List<Option>> optionCreator;

  public OptionGroup(String description, Provider<List<Option>> optionCreator) {
    super(description);

    this.optionCreator = optionCreator;
  }

  public List<Option> getOptions() {
    return optionCreator.get();
  }
}