package hs.mediasystem.screens;

import hs.mediasystem.ProgramController;
import hs.mediasystem.SelectItemScene;
import hs.mediasystem.fs.MoviesMediaTree;

import java.nio.file.Paths;

import javafx.scene.Node;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class MoviesMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectItemScene> selectItemSceneProvider;

  @Inject
  public MoviesMainMenuExtension(Provider<SelectItemScene> selectItemSceneProvider) {
    this.selectItemSceneProvider = selectItemSceneProvider;
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
    Node scene = selectItemSceneProvider.get().create(new MoviesMediaTree(Paths.get(controller.getIni().getValue("general", "movies.path"))));

    controller.showScreen(scene);
  }
}
