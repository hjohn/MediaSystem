package hs.mediasystem.screens;

import hs.mediasystem.db.TvdbEpisodeEnricher;
import hs.mediasystem.db.TvdbSerieEnricher;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.fs.SeriesMediaTree;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;

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

    TypeBasedItemEnricher.registerEnricher("SERIE", serieEnricher);
    TypeBasedItemEnricher.registerEnricher("EPISODE", new TvdbEpisodeEnricher(serieEnricher));
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
        mediaTree = new SeriesMediaTree(Paths.get(controller.getIni().getValue("general", "series.path")));
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
    return 0.2;
  }
}
