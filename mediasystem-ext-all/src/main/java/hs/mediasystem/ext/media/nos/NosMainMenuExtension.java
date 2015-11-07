package hs.mediasystem.ext.media.nos;

import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.collection.CollectionLocation;
import javafx.scene.image.Image;

import javax.inject.Named;

@Named
public class NosMainMenuExtension implements MainMenuExtension {

  @Override
  public String getTitle() {
    return "NOS";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/media/nos/nos.png"));
  }

  @Override
  public void select(final ProgramController controller) {
    controller.setLocation(new CollectionLocation(new NosMediaTree()));
  }

  @Override
  public double order() {
    return 0.4;
  }
}
