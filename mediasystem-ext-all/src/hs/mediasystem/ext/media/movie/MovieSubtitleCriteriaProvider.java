package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleCriteriaProvider;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

@Named
public class MovieSubtitleCriteriaProvider implements SubtitleCriteriaProvider {

  @Override
  public Map<String, Object> getCriteria(MediaItem mediaItem) {
    Media media = mediaItem.getMedia();
    MediaData mediaData = mediaItem.mediaData.get();

    Map<String, Object> criteria = new HashMap<>();

    criteria.put(SubtitleCriteriaProvider.TITLE, media.title.get());

    if(media instanceof Movie) {
      Movie movie = (Movie)media;

      criteria.put(SubtitleCriteriaProvider.YEAR, movie.releaseDate.get() == null ? null : movie.releaseDate.get().getYear());
      criteria.put(SubtitleCriteriaProvider.IMDB_ID, movie.imdbNumber.get());
    }

    if(mediaData != null) {
      criteria.put(SubtitleCriteriaProvider.OPEN_SUBTITLES_HASH, mediaData.osHash.get());
      criteria.put(SubtitleCriteriaProvider.FILE_LENGTH, mediaData.fileLength.get());
    }

    return criteria;
  }

  @Override
  public Class<?> getMediaType() {
    return Movie.class;
  }
}
