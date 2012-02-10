package hs.mediasystem.screens;

import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.MoviesMediaTree;
import hs.mediasystem.fs.SeriesMediaTree;

public class StandardLayout {

  public CellProvider<MediaItem> getCellProvider(MediaTree mediaTree) {
    if(mediaTree instanceof MoviesMediaTree) {
      return getCellProvider(MediaType.MOVIE);
    }
    else if(mediaTree instanceof SeriesMediaTree) {
      return getCellProvider(MediaType.SERIE);
    }

    return getCellProvider(MediaType.SEASON);
  }

  public CellProvider<MediaItem> getCellProvider(MediaType mediaType) {
    if(mediaType == MediaType.MOVIE) {
      return new MovieCellProvider();
    }
    else if(mediaType == MediaType.SERIE) {
      return new BannerRenderer();
    }

    return new SeasonAndEpisodeCellProvider();
  }
}
