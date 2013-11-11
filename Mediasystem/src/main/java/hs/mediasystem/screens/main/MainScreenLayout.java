package hs.mediasystem.screens.main;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.Location;
import hs.mediasystem.screens.MainLocationPresentation;
import hs.mediasystem.screens.ProgramController;
import javafx.scene.Node;

@Singleton
public class MainScreenLayout implements Layout<Location, MainLocationPresentation<Location>> {
  private final Provider<MainScreen> mainScreenProvider;
  private final ProgramController programController;

  @Inject
  public MainScreenLayout(ProgramController programController, Provider<MainScreen> mainScreenProvider) {
    this.programController = programController;
    this.mainScreenProvider = mainScreenProvider;
  }

  @Override
  public Class<?> getContentClass() {
    return MainScreenLocation.class;
  }

  @Override
  public MainLocationPresentation<Location> createPresentation() {
    return new MainLocationPresentation<>(programController);
  }

  @Override
  public Node createView(MainLocationPresentation<Location> presentation) {
    return mainScreenProvider.get();
  }
}
