package hs.mediasystem.ext.config;

import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.SettingGroup;
import javafx.scene.image.Image;

import org.osgi.framework.BundleContext;

public class ConfigMainMenuExtension implements MainMenuExtension {
  private volatile BundleContext bundleContext;

  private SettingGroup settingGroup;

  public void init() {
    settingGroup = new SettingGroup(bundleContext, null, "Configuration", 0);
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
    controller.showOptionScreen(getTitle(), settingGroup.createOption().getOptions());
  }
}
