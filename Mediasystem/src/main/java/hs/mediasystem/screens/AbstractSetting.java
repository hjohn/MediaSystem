package hs.mediasystem.screens;

import java.util.Set;

import hs.mediasystem.screens.optiondialog.Option;

public abstract class AbstractSetting implements Setting {
  private final String id;
  private final String parentId;
  private final double order;

  public AbstractSetting(String id, String parentId, double order) {
    this.id = id;
    this.parentId = parentId;
    this.order = order;
  }

  @Override
  public final String getId() {
    return id;
  }

  @Override
  public String getParentId() {
    return parentId;
  }

  @Override
  public final double order() {
    return order;
  }

  @Override
  public abstract Option createOption(Set<Setting> settings);
}
