package hs.mediasystem.ext.movie;

import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaDataEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaNodeCellProviderRegistry;
import hs.mediasystem.fs.MediaItemComparator;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.media.Media;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.StandardLayout;
import hs.mediasystem.screens.StandardLayout.MediaGroup;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;
import hs.mediasystem.screens.selectmedia.StandardView;

import java.nio.file.Paths;

import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class MoviesMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectMediaPresentation> selectMediaPresentationProvider;
  private final EnrichCache enrichCache;

  @Inject
  public MoviesMainMenuExtension(Provider<SelectMediaPresentation> selectMediaPresentationProvider, MediaDataEnricher identifierEnricher, EnrichCache enrichCache, MovieEnricher movieEnricher) {
    this.selectMediaPresentationProvider = selectMediaPresentationProvider;
    this.enrichCache = enrichCache;

    TypeBasedItemEnricher.registerEnricher(Movie.class, new TmdbMovieEnricher());
    StandardView.registerLayout(MoviesMediaTree.class, MediaRootType.MOVIES);
    MediaNodeCellProviderRegistry.register(MediaNodeCellProviderRegistry.HORIZONTAL_CELL, Movie.class, new Provider<MovieCell>() {
      @Override
      public MovieCell get() {
        return new MovieCell();
      }
    });

    StandardLayout.registerMediaGroup(MoviesMediaTree.class, new MediaGroup(new MovieGrouper(), MediaItemComparator.INSTANCE, false, false) {
      @Override
      public Media createMediaFromFirstItem(MediaItem item) {
        return new Media(item.getTitle(), null, item.getMedia().getReleaseYear());
      }
    });

    enrichCache.registerEnricher(MediaData.class, identifierEnricher);
    enrichCache.registerEnricher(Movie.class, movieEnricher);
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
      }

      @Override
      protected void intro() {
        controller.showScreen(presentation.getView());
        if(mediaTree == null) {
          mediaTree = new MoviesMediaTree(enrichCache, Paths.get(controller.getIni().getValue("general", "movies.path")));
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
