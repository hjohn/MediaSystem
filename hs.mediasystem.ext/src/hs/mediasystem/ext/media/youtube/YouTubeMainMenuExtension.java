package hs.mediasystem.ext.media.youtube;

import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaLocation;
import hs.mediasystem.screens.selectmedia.StandardView;
import javafx.scene.image.Image;

public class YouTubeMainMenuExtension implements MainMenuExtension {

  public YouTubeMainMenuExtension() {
    StandardView.registerLayout(YouTubeMediaTree.class, MediaRootType.MOVIES);
    StandardView.registerLayout(YouTubeFeed.class, MediaRootType.MOVIES);
  }

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
    controller.setLocation(new SelectMediaLocation(new YouTubeMediaTree()));
  }

  @Override
  public double order() {
    return 0.3;
  }
}
