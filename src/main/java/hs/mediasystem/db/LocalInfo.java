package hs.mediasystem.db;

public class LocalInfo<T> {
  private final String uri;
  private final String mediaType;
  private final String groupName;
  private final String title;
  private final String subtitle;
  private final String code;
  private final Integer releaseYear;
  private final Integer season;
  private final Integer episode;
  private final T userData;

  public LocalInfo(String uri, String mediaType, String groupName, String title, String subtitle, String code, Integer releaseYear, Integer season, Integer episode, T userData) {
    this.uri = uri;
    this.mediaType = mediaType;
    this.groupName = groupName;
    this.title = title;
    this.subtitle = subtitle;
    this.code = code;
    this.releaseYear = releaseYear;
    this.season = season;
    this.episode = episode;
    this.userData = userData;
  }

  public LocalInfo(String mediaType, String title, Integer releaseYear) {
    this(null, mediaType, null, title, null, null, releaseYear, null, null, null);
  }

  public LocalInfo(String mediaType, String title) {
    this(null, mediaType, null, title, null, null, null, null, null, null);
  }

  public String getSurrogateName() {
    return mediaType + "/" + (groupName != null ? groupName.toLowerCase() : "") + "/" + (title != null ? title.toLowerCase() : "") + "/" + (subtitle != null ? subtitle.toLowerCase() : "") + "/" + (season == null ? "" : season) + "/" + (episode == null ? "" : episode) + "/" + (releaseYear == null ? "" : releaseYear);
  }

  public T getUserData() {
    return userData;
  }

  public String getUri() {
    return uri;
  }

  public String getType() {
    return mediaType;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getTitle() {
    return title;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getCode() {
    return code;
  }

  public Integer getReleaseYear() {
    return releaseYear;
  }

  public Integer getSeason() {
    return season;
  }

  public Integer getEpisode() {
    return episode;
  }

  @Override
  public String toString() {
    return "LocalInfo('" + title + "', type=" + mediaType + ", season=" + season + ", ep=" + episode + ", uri=" + uri + ")";
  }
}
