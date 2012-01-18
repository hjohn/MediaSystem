package hs.mediasystem.screens;

import hs.mediasystem.fs.MoviesMediaTree;

import java.nio.file.Paths;

import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class MoviesMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectMediaPresentation> selectMediaPresentationProvider;

  @Inject
  public MoviesMainMenuExtension(Provider<SelectMediaPresentation> selectMediaPresentationProvider) {
    this.selectMediaPresentationProvider = selectMediaPresentationProvider;
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
  public void select(ProgramController controller) {
    SelectMediaPresentation presentation = selectMediaPresentationProvider.get();

    presentation.setMediaTree(new MoviesMediaTree(Paths.get(controller.getIni().getValue("general", "movies.path"))));

    controller.showScreen(presentation.getView());
  }
}
