package hs.mediasystem.screens;

import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.OptionGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

public class SettingGroup implements Setting {
  private static final Comparator<Setting> SETTING_COMPARATOR = new Comparator<Setting>() {
    @Override
    public int compare(Setting o1, Setting o2) {
      int result = Double.compare(o1.order(), o2.order());

      if(result == 0) {
        result = o1.getId().compareTo(o2.getId());
      }

      return result;
    }
  };

  private final String id;
  private final String parentId;
  private final String title;
  private final double order;

  public SettingGroup(String id, String parentId, String title, double order) {
    this.id = id;
    this.parentId = parentId;
    this.title = title;
    this.order = order;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getParentId() {
    return parentId;
  }

  @Override
  public double order() {
    return order;
  }

  @Override
  public OptionGroup createOption(final Set<Setting> settings) {
    return new OptionGroup(title, new Provider<List<Option>>() {
      @Override
      public List<Option> get() {
        return createChildOptions(settings);
      }
    });
  }

  private List<Option> createChildOptions(Set<Setting> settings) {
    List<Setting> matchedSettings = new ArrayList<>();

    for(Setting setting : settings) {
      if((getId() == null && setting.getParentId() == null) || (getId() != null && getId().equals(setting.getParentId()))) {
        matchedSettings.add(setting);
      }
    }

    Collections.sort(matchedSettings, SETTING_COMPARATOR);

    List<Option> options = new ArrayList<>();

    for(final Setting setting : matchedSettings) {
      options.add(setting.createOption(settings));
    }

    return options;
  }
}
