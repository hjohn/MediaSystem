package hs.mediasystem.ext.shutdown;

import javafx.scene.image.Image;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;

public class ShutdownMainMenuExtension implements MainMenuExtension {

  @Override
  public double order() {
    return 1.0;
  }

  @Override
  public String getTitle() {
    return "Shutdown";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/shutdown/shutdown.png"));
  }

  @Override
  public Destination getDestination(ProgramController controller) {
    return null;
  }

}
