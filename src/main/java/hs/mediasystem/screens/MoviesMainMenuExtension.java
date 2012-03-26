package hs.mediasystem.screens;

import hs.mediasystem.db.TmdbMovieEnricher;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.fs.MoviesMediaTree;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;

import java.nio.file.Paths;

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

    TypeBasedItemEnricher.registerEnricher("MOVIE", new TmdbMovieEnricher());
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
  public Destination getDestination(final ProgramController controller) {
    return new Destination(getTitle()) {
      private SelectMediaPresentation presentation;
      private MoviesMediaTree mediaTree;

      @Override
      protected void init() {
        presentation = selectMediaPresentationProvider.get();
        mediaTree = new MoviesMediaTree(Paths.get(controller.getIni().getValue("general", "movies.path")));
        mediaTree.onItemQueued().set(enrichmentHandler);
      }

      @Override
      protected void intro() {
        controller.showScreen(presentation.getView());
      }

      @Override
      protected void execute() {
        presentation.setMediaTree(mediaTree);
      }
    };
  }

  @Override
  public double order() {
    return 0.1;
  }
}
