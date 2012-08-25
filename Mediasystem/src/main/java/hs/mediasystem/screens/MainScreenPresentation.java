package hs.mediasystem.screens;

import javafx.scene.Node;

public class MainScreenPresentation implements Presentation {
  private final MainScreen mainScreen;

  public MainScreenPresentation(ProgramController controller) {
    mainScreen = new MainScreen(controller);
  }

  @Override
  public Node getView() {
    return mainScreen;
  }

  @Override
  public void dispose() {
  }
}
