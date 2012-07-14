package hs.mediasystem.screens;

import hs.mediasystem.screens.optiondialog.Option;

import javax.inject.Provider;

public class SimpleSetting implements Setting {
  private final String id;
  private final double order;
  private final Provider<Option> optionProvider;

  public SimpleSetting(String id, double order, Provider<Option> optionProvider) {
    this.id = id;
    this.order = order;
    this.optionProvider = optionProvider;
  }

  @Override
  public final String getId() {
    return id;
  }

  @Override
  public final double order() {
    return order;
  }

  @Override
  public Option createOption() {
    return optionProvider == null ? null : optionProvider.get();
  }
}
