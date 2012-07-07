package hs.mediasystem.ext.config;

import javafx.scene.image.Image;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;

public class ConfigMainMenuExtension implements MainMenuExtension {

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
  public Destination getDestination(ProgramController controller) {
    return null;
  }

}
