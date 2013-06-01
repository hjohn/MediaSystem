package hs.mediasystem.framework;

import java.util.Map;

public interface SubtitleCriteriaProvider {
  static final String TITLE = "string:title";
  static final String YEAR = "int:releaseYear";
  static final String SEASON = "int:season";
  static final String EPISODE = "int:episode";
  static final String IMDB_ID = "string:imdbId";
  static final String OPEN_SUBTITLES_HASH = "long:oshash";
  static final String FILE_LENGTH = "long:fileLength";

  Map<String, Object> getCriteria(MediaItem mediaItem);
  Class<?> getMediaType();
}
