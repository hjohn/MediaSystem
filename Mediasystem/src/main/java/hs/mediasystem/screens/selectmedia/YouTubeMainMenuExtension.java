package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.fs.YouTubeFeed;
import hs.mediasystem.fs.YouTubeMediaTree;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class YouTubeMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectMediaPresentation> selectMediaPresentationProvider;

  @Inject
  public YouTubeMainMenuExtension(Provider<SelectMediaPresentation> selectMediaPresentationProvider) {
    this.selectMediaPresentationProvider = selectMediaPresentationProvider;

    StandardView.registerLayout(YouTubeMediaTree.class, MediaRootType.MOVIES);
    StandardView.registerLayout(YouTubeFeed.class, MediaRootType.MOVIES);
  }

  @Override
  public String getTitle() {
    return "YouTube";
  }

  @Override
  public Image getImage() {
    return new Image("images/video.png");
  }

  @Override
  public Destination getDestination(final ProgramController controller) {
    return new Destination(getTitle()) {
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
