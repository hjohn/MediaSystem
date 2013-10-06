package hs.mediasystem.screens.main;

import hs.mediasystem.screens.Presentation;
import javafx.scene.Node;

import javax.inject.Inject;

public class MainScreenPresentation implements Presentation {
  private final MainScreen mainScreen;

  @Inject
  public MainScreenPresentation(MainScreen mainScreen) {
    this.mainScreen = mainScreen;
  }

  @Override
  public Node getView() {
    return mainScreen;
  }

  @Override
  public void dispose() {
  }
}
