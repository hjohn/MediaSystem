package hs.mediasystem.framework;

import java.util.Map;

public interface SubtitleCriteriaProvider {
  public static final String TITLE = "string:title";
  public static final String SEASON = "int:season";
  public static final String EPISODE = "int:episode";
  public static final String IMDB_ID = "string:imdbId";
  public static final String OPEN_SUBTITLES_HASH = "long:oshash";
  public static final String FILE_LENGTH = "long:fileLength";

  Map<String, Object> getCriteria(MediaItem mediaItem);
}
