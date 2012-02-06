package hs.mediasystem.screens;

import hs.mediasystem.fs.MediaItemEnrichmentEventHandler;
import hs.mediasystem.fs.MoviesMediaTree;

import java.nio.file.Paths;

import javafx.scene.Node;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class MoviesMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectMediaPresentation> selectMediaPresentationProvider;
  private final MediaItemEnrichmentEventHandler enrichmentHandler;

  @Inject
  public MoviesMainMenuExtension(Provider<SelectMediaPresentation> selectMediaPresentationProvider, MediaItemEnrichmentEventHandler enrichmentHandler) {
    this.selectMediaPresentationProvider = selectMediaPresentationProvider;
    this.enrichmentHandler = enrichmentHandler;
  }

  @Override
  public String getTitle() {
    return "Movies";
  }

  @Override
  public Image getImage() {
    return new Image("images/package_multimedia.png");
  }

  @Override
  public Node select(ProgramController controller) {
    SelectMediaPresentation presentation = selectMediaPresentationProvider.get();
    MoviesMediaTree mediaTree = new MoviesMediaTree(Paths.get(controller.getIni().getValue("general", "movies.path")));

    mediaTree.onItemQueued().set(enrichmentHandler);

    presentation.setMediaTree(mediaTree);

    return presentation.getView();
  }
}
