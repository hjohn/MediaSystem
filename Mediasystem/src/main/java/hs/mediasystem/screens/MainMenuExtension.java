package hs.mediasystem.screens;

import hs.mediasystem.screens.Navigator.Destination;
import javafx.scene.image.Image;

public interface MainMenuExtension {
  double order();
  String getTitle();
  Image getImage();
  Destination getDestination(ProgramController controller);
}
