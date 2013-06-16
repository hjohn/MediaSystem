package hs.mediasystem.ext.media.movie;

import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.collection.CollectionLocation;
import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class MoviesMainMenuExtension implements MainMenuExtension {
  private final Provider<MoviesMediaTree> moviesMediaTreeProvider;

  @Inject
  public MoviesMainMenuExtension(Provider<MoviesMediaTree> moviesMediaTreeProvider) {
    this.moviesMediaTreeProvider = moviesMediaTreeProvider;
  }

  @Override
  public String getTitle() {
    return "Movies";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/media/movie/movie.png"));
  }

  @Override
  public void select(final ProgramController controller) {
    controller.setLocation(new CollectionLocation(moviesMediaTreeProvider.get()));
  }

  @Override
  public double order() {
    return 0.1;
  }
}
