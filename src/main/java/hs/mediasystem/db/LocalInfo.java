package hs.mediasystem.db;

import java.nio.file.Path;

public class LocalInfo {
  private final Path path;
  private final MediaType mediaType;
  private final String groupName;
  private final String title;
  private final String subtitle;
  private final String code;
  private final Integer releaseYear;
  private final Integer season;
  private final Integer episode;

  public LocalInfo(Path path, MediaType mediaType, String groupName, String title, String subtitle, String code, Integer releaseYear, Integer season, Integer episode) {
    this.path = path;
    this.mediaType = mediaType;
    this.groupName = groupName;
    this.title = title;
    this.subtitle = subtitle;
    this.code = code;
    this.releaseYear = releaseYear;
    this.season = season;
    this.episode = episode;
  }

  public LocalInfo(MediaType mediaType, String title, Integer releaseYear) {
    this(null, mediaType, null, title, null, null, releaseYear, null, null);
  }

  public LocalInfo(MediaType mediaType, String title) {
    this(null, mediaType, null, title, null, null, null, null, null);
  }

  public String getSurrogateName() {
    return mediaType.name() + "/" + (groupName != null ? groupName.toLowerCase() : "") + "/" + (title != null ? title.toLowerCase() : "") + "/" + (subtitle != null ? subtitle.toLowerCase() : "") + "/" + (season == null ? "" : season) + "/" + (episode == null ? "" : episode) + "/" + (releaseYear == null ? "" : releaseYear);
  }

  public Path getPath() {
    return path;
  }

  public MediaType getType() {
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
    return "LocalInfo('" + title + "', type=" + mediaType.name() + ", season=" + season + ", ep=" + episode + ", path=" + path + ")";
  }
}
