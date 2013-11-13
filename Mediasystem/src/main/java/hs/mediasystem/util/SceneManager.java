package hs.mediasystem.util;

import javafx.scene.Scene;

public interface SceneManager {
  void setScene(Scene scene);
  void setPlayerRoot(Object root);
  void disposePlayerRoot();

  int getScreenNumber();
  void setScreenNumber(int screenNumber);
}
