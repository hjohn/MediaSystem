package hs.mediasystem.ext.media.youtube;

import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.StandardView;
import javafx.scene.image.Image;

public class YouTubeMainMenuExtension implements MainMenuExtension {
  private volatile SelectMediaPresentationProvider selectMediaPresentationProvider;

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
  public Destination getDestination(final ProgramController controller) {
    return new Destination("youtube", getTitle()) {
      private SelectMediaPresentation presentation;
      private YouTubeMediaTree mediaTree;

      @Override
      protected void init() {
        presentation = selectMediaPresentationProvider.get();
      }

      @Override
      protected void intro() {
        controller.showScreen(presentation.getView());
        if(mediaTree == null) {
          mediaTree = new YouTubeMediaTree();
          presentation.setMediaTree(mediaTree);
        }
      }
    };
  }

  @Override
  public double order() {
    return 0.3;
  }
}
