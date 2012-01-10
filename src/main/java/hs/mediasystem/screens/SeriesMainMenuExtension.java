package hs.mediasystem.screens;

import hs.mediasystem.ProgramController;
import hs.mediasystem.SelectItemScene;
import hs.mediasystem.fs.SeriesMediaTree;

import java.nio.file.Paths;

import javafx.scene.Node;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class SeriesMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectItemScene> selectItemSceneProvider;

  @Inject
  public SeriesMainMenuExtension(Provider<SelectItemScene> selectItemSceneProvider) {
    this.selectItemSceneProvider = selectItemSceneProvider;
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
  public void select(ProgramController controller) {
//    ItemEnricher itemEnricher = new CachedItemEnricher(new ItemsDao(), new TvdbSerieEnricher());

    Node scene = selectItemSceneProvider.get().create(new SeriesMediaTree(Paths.get(controller.getIni().getValue("general", "series.path"))));

    controller.showScreen(scene);
  }
}
