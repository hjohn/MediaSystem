package hs.mediasystem.screens;

import javafx.scene.image.Image;

public interface MainMenuExtension {
  double order();
  String getTitle();
  Image getImage();
  void select(ProgramController controller);
}
