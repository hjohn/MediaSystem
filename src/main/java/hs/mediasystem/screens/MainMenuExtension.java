package hs.mediasystem.screens;

import javafx.scene.Node;
import javafx.scene.image.Image;

public interface MainMenuExtension {
  String getTitle();
  Image getImage();
  Node select(ProgramController controller);
}
