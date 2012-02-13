package hs.mediasystem.screens;

import hs.mediasystem.fs.SeriesMediaTree;

import java.nio.file.Paths;

import javafx.scene.Node;
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
  public Node select(ProgramController controller) {
    SelectMediaPresentation presentation = selectMediaPresentationProvider.get();
    SeriesMediaTree mediaTree = new SeriesMediaTree(Paths.get(controller.getIni().getValue("general", "series.path")));

    mediaTree.onItemQueued().set(enrichmentHandler);

    presentation.setMediaTree(mediaTree);

    return presentation.getView();
  }

  @Override
  public double order() {
    return 0.2;
  }
}
