package hs.mediasystem.screens;

import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.db.YouTubeEnricher;
import hs.mediasystem.fs.YouTubeMediaTree;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;
import javafx.scene.Node;
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

    TypeBasedItemEnricher.registerEnricher("YOUTUBE", new YouTubeEnricher());
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
  public Node select(ProgramController controller) {
    SelectMediaPresentation presentation = selectMediaPresentationProvider.get();
    YouTubeMediaTree mediaTree = new YouTubeMediaTree();

    mediaTree.onItemQueued().set(enrichmentHandler);

    presentation.setMediaTree(mediaTree);

    return presentation.getView();
  }

  @Override
  public double order() {
    return 0.3;
  }
}