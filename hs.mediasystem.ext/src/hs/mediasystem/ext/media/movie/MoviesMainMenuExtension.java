package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaNodeCellProviderRegistry;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.StandardView;
import hs.mediasystem.util.CryptoUtil;

import java.nio.file.Paths;

import javafx.scene.image.Image;

import javax.inject.Provider;

import net.sf.jtmdb.GeneralSettings;

public class MoviesMainMenuExtension implements MainMenuExtension {
  private volatile SelectMediaPresentationProvider selectMediaPresentationProvider;
  private volatile MovieEnricher movieEnricher;
  private volatile EnrichCache enrichCache;
  private volatile PersistQueue persister;

  public MoviesMainMenuExtension() {
    GeneralSettings.setApiKey(CryptoUtil.decrypt("8AF22323DB8C0F235B38F578B7E09A61DB6F971EED59DE131E4EF70003CE84B483A778EBD28200A031F035F4209B61A4", "-MediaSystem-"));
    GeneralSettings.setLogEnabled(false);
    GeneralSettings.setLogStream(System.out);

    TypeBasedItemEnricher.registerEnricher(MovieBase.class, new TmdbMovieEnricher());
    StandardView.registerLayout(MoviesMediaTree.class, MediaRootType.MOVIES);
    MediaNodeCellProviderRegistry.register(MediaNodeCellProviderRegistry.HORIZONTAL_CELL, Movie.class, new Provider<MovieCell>() {
      @Override
      public MovieCell get() {
        return new MovieCell();
      }
    });
  }

  public void init() {
    enrichCache.registerEnricher(Movie.class, movieEnricher);
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
  public Destination getDestination(final ProgramController controller) {
    return new Destination("movie", getTitle()) {
      private SelectMediaPresentation presentation;
      private MoviesMediaTree mediaTree;

      @Override
      protected void init() {
        presentation = selectMediaPresentationProvider.get();
      }

      @Override
      protected void intro() {
        controller.showScreen(presentation.getView());
        if(mediaTree == null) {
          mediaTree = new MoviesMediaTree(enrichCache, persister, Paths.get(controller.getIni().getValue("general", "movies.path")));
          presentation.setMediaTree(mediaTree);
        }
      }
    };
  }

  @Override
  public double order() {
    return 0.1;
  }
}
