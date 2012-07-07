package hs.mediasystem.ext.config;

import hs.mediasystem.screens.ConfigurationOption;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.util.ServiceTracker;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

import org.osgi.framework.BundleContext;

public class ConfigMainMenuExtension implements MainMenuExtension {
  private volatile BundleContext bundleContext;

  private ServiceTracker<ConfigurationOption> configurationOptionTracker;

  public void init() {
    configurationOptionTracker = new ServiceTracker<>(bundleContext, ConfigurationOption.class);
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
  public Destination getDestination(final ProgramController controller) {
    return new Destination("configuration", getTitle()) {

      @Override
      protected void intro() {
        controller.showOptionScreen(getTitle(), createOptionsWithParentId(null));
      }
    };
  }

  private List<Option> createOptionsWithParentId(String parentId) {
    List<ConfigurationOption> configOptions = configurationOptionTracker.getServices();
    List<ConfigurationOption> foundConfigOptions = new ArrayList<>();

    for(ConfigurationOption option : configOptions) {
      if((parentId == null && option.getParentId() == null) || (parentId != null && parentId.equals(option.getParentId()))) {
        foundConfigOptions.add(option);
      }
    }

    // TODO sort

    List<Option> options = new ArrayList<>();

    for(ConfigurationOption option : foundConfigOptions) {
      options.add(option.createOption());
    }

    return options;
  }
}
