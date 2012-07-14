package hs.mediasystem.screens;

import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.SubOption;
import hs.mediasystem.util.PropertyEq;
import hs.mediasystem.util.ServiceTracker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import org.osgi.framework.BundleContext;

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
  private final String title;
  private final double order;
  private final ServiceTracker<Setting> childSettingTracker;

  public SettingGroup(BundleContext bundleContext, String id, String title, double order) {
    this.id = id;
    this.title = title;
    this.order = order;

    childSettingTracker = new ServiceTracker<>(bundleContext, Setting.class, SETTING_COMPARATOR, new PropertyEq("parentId", id));
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public double order() {
    return order;
  }

  @Override
  public SubOption createOption() {
    return new SubOption(title, new Provider<List<Option>>() {
      @Override
      public List<Option> get() {
        return createChildOptions();
      }
    });
  }

  private List<Option> createChildOptions() {
    List<Option> options = new ArrayList<>();

    for(final Setting setting : childSettingTracker.getServices()) {
      options.add(setting.createOption());
    }

    return options;
  }
}
