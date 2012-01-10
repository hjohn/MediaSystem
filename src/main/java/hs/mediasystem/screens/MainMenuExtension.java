package hs.mediasystem.screens;

import hs.mediasystem.ProgramController;
import javafx.scene.image.Image;

public interface MainMenuExtension {
  String getTitle();
  Image getImage();
  void select(ProgramController controller);
}
