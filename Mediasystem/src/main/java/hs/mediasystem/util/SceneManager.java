package hs.mediasystem.util;

import java.awt.Component;

import javafx.scene.Scene;

public interface SceneManager {
  void setScene(Scene scene);
  void setPlayerRoot(Component root);
  void disposePlayerRoot();

  int getScreenNumber();
  void setScreenNumber(int screenNumber);

  void displayDialog(DialogStage stage);
  void displaySynchronousDialog(DialogStage stage);
}
