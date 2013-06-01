package hs.mediasystem.ext.config;

import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.Setting;
import hs.mediasystem.screens.SettingGroup;
import hs.mediasystem.screens.optiondialog.OptionDialogPane;

import java.util.Set;

import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ConfigMainMenuExtension implements MainMenuExtension {
  private final Set<Setting> settings;
  private final SettingGroup settingGroup = new SettingGroup(null, null, "Configuration", 0);

  @Inject
  public ConfigMainMenuExtension(Set<Setting> settings) {
    this.settings = settings;
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
    controller.showDialog(new OptionDialogPane("Configuration", settingGroup.createOption(settings).getOptions()));
  }
}
