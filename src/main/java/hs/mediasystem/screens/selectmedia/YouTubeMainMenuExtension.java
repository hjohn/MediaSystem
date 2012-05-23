package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.db.YouTubeEnricher;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.fs.YouTubeMediaTree;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MediaItemEnrichmentEventHandler;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class YouTubeMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectMediaPresentation> selectMediaPresentationProvider;
  private final MediaItemEnrichmentEventHandler enrichmentHandler;

  @Inject
  public YouTubeMainMenuExtension(Provider<SelectMediaPresentation> selectMediaPresentationProvider, MediaItemEnrichmentEventHandler enrichmentHandler) {
    this.selectMediaPresentationProvider = selectMediaPresentationProvider;
    this.enrichmentHandler = enrichmentHandler;

    TypeBasedItemEnricher.registerEnricher("YouTube", new YouTubeEnricher());
    StandardView.registerLayout(YouTubeMediaTree.class, MediaRootType.MOVIES);
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
          mediaTree.onItemQueued().set(enrichmentHandler);
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
