package hs.mediasystem.ext.media.youtube;

import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.collection.CollectionLocation;
import javafx.scene.image.Image;

import javax.inject.Named;

@Named
public class YouTubeMainMenuExtension implements MainMenuExtension {

  @Override
  public String getTitle() {
    return "YouTube";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/media/youtube/youtube.png"));
  }

  @Override
  public void select(final ProgramController controller) {
    controller.setLocation(new CollectionLocation(new YouTubeMediaTree()));
  }

  @Override
  public double order() {
    return 0.3;
  }
}
