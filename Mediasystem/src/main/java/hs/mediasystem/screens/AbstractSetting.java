package hs.mediasystem.screens;

import hs.mediasystem.screens.optiondialog.Option;

public abstract class AbstractSetting implements Setting {
  private final String id;
  private final double order;

  public AbstractSetting(String id, double order) {
    this.id = id;
    this.order = order;
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
  public abstract Option createOption();
}
