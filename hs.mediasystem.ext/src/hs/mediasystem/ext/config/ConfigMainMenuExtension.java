package hs.mediasystem.ext.config;

import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.Setting;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.SubOption;
import hs.mediasystem.util.ServiceTracker;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

import javax.inject.Provider;

import org.osgi.framework.BundleContext;

public class ConfigMainMenuExtension implements MainMenuExtension {
  private volatile BundleContext bundleContext;

  private ServiceTracker<Setting> configurationOptionTracker;

  public void init() {
    configurationOptionTracker = new ServiceTracker<>(bundleContext, Setting.class);
  }

  @Override
  public double order() {
    return 0.9;
  }

  @Override
  public String getTitle() {
    return "Configuration";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/config/config.png"));
  }

  @Override
  public void select(final ProgramController controller) {
    controller.showOptionScreen(getTitle(), createOptionsWithParentId(null));
  }

  private List<Option> createOptionsWithParentId(String parentId) {
    List<Setting> configOptions = configurationOptionTracker.getServices();
    List<Setting> foundConfigOptions = new ArrayList<>();

    for(Setting option : configOptions) {
      if((parentId == null && option.getParentId() == null) || (parentId != null && parentId.equals(option.getParentId()))) {
        foundConfigOptions.add(option);
      }
    }

    // TODO sort

    List<Option> options = new ArrayList<>();

    for(final Setting configOption : foundConfigOptions) {
      Option option = configOption.createOption();

      if(option == null) {
        option = new SubOption(configOption.getTitle(), new Provider<List<Option>>() {
          @Override
          public List<Option> get() {
            return createOptionsWithParentId(configOption.getId());
          }
        });
      }

      options.add(option);
    }

    return options;
  }
}
