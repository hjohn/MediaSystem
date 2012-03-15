package hs.mediasystem;

import java.awt.Component;

import javafx.scene.Scene;

public interface SceneManager {
  void setScene(Scene scene);
  void setPlayerRoot(Component root);

  int getScreenNumber();
  void setScreenNumber(int screenNumber);
}
