package hs.mediasystem.screens;

import javafx.scene.Node;

public interface Presentation {
  Node getView();
  void dispose();
}
