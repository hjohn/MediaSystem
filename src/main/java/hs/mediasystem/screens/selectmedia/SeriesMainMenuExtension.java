package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.db.TvdbEpisodeEnricher;
import hs.mediasystem.db.TvdbSerieEnricher;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.fs.SeriesMediaTree;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MediaItemEnrichmentEventHandler;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;

import java.nio.file.Paths;

import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class SeriesMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectMediaPresentation> selectMediaPresentationProvider;
  private final MediaItemEnrichmentEventHandler enrichmentHandler;

  @Inject
  public SeriesMainMenuExtension(Provider<SelectMediaPresentation> selectMediaPresentationProvider, MediaItemEnrichmentEventHandler enrichmentHandler) {
    this.selectMediaPresentationProvider = selectMediaPresentationProvider;
    this.enrichmentHandler = enrichmentHandler;

    TvdbSerieEnricher serieEnricher = new TvdbSerieEnricher();

    TypeBasedItemEnricher.registerEnricher("Serie", serieEnricher);
    TypeBasedItemEnricher.registerEnricher("Episode", new TvdbEpisodeEnricher(serieEnricher));
  }

  @Override
  public String getTitle() {
    return "Series";
  }

  @Override
  public Image getImage() {
    return new Image("images/aktion.png");
  }

  @Override
  public Destination getDestination(final ProgramController controller) {
    return new Destination(getTitle()) {
      private SelectMediaPresentation presentation;
      private SeriesMediaTree mediaTree;

      @Override
      protected void init() {
        presentation = selectMediaPresentationProvider.get();
      }

      @Override
      protected void intro() {
        controller.showScreen(presentation.getView());
        if(mediaTree == null) {
          mediaTree = new SeriesMediaTree(Paths.get(controller.getIni().getValue("general", "series.path")));
          mediaTree.onItemQueued().set(enrichmentHandler);
          presentation.setMediaTree(mediaTree);
        }
      }
    };
  }

  @Override
  public double order() {
    return 0.2;
  }
}
