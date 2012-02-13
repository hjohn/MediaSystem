package hs.mediasystem.screens;

import javafx.scene.Node;
import javafx.scene.image.Image;

public interface MainMenuExtension {
  double order();
  String getTitle();
  Image getImage();
  Node select(ProgramController controller);
}
