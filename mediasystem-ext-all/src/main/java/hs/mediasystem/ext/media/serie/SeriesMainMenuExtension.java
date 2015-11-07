package hs.mediasystem.ext.media.serie;

import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.collection.CollectionLocation;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

@Named
public class SeriesMainMenuExtension implements MainMenuExtension {
  private final Provider<SeriesMediaTree> seriesMediaTreeProvider;

  @Inject
  public SeriesMainMenuExtension(Provider<SeriesMediaTree> seriesMediaTreeProvider) {
    this.seriesMediaTreeProvider = seriesMediaTreeProvider;
  }

  @Override
  public String getTitle() {
    return "Series";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/media/serie/serie.png"));
  }

  @Override
  public void select(final ProgramController controller) {
    controller.setLocation(new CollectionLocation(seriesMediaTreeProvider.get()));
  }

  @Override
  public double order() {
    return 0.2;
  }
}
