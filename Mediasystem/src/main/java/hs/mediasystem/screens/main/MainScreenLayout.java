package hs.mediasystem.screens.main;

import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.Location;
import hs.mediasystem.screens.MainLocationPresentation;
import javafx.scene.Node;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class MainScreenLayout implements Layout<Location, MainLocationPresentation<Location>> {
  private final Provider<MainScreen> mainScreenProvider;

  @Inject
  public MainScreenLayout(Provider<MainScreen> mainScreenProvider) {
    this.mainScreenProvider = mainScreenProvider;
  }

  @Override
  public Class<?> getContentClass() {
    return MainScreenLocation.class;
  }

  @Override
  public MainLocationPresentation<Location> createPresentation() {
    return new MainLocationPresentation<>();
  }

  @Override
  public Node createView(MainLocationPresentation<Location> presentation) {
    return mainScreenProvider.get();
  }
}
