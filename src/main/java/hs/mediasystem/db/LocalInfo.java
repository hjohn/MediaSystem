package hs.mediasystem.db;

import java.nio.file.Path;

public class LocalInfo {
  private final Path path;
  private final Type type;
  private final String title;
  private final String subtitle;
  private final String code;
  private final Integer releaseYear;
  private final Integer season;
  private final Integer episode;

  public enum Type {MOVIE, SERIE, EPISODE}

  public LocalInfo(Path path, Type type, String title, String subtitle, String code, Integer releaseYear, Integer season, Integer episode) {
    this.path = path;
    this.type = type;
    this.title = title;
    this.subtitle = subtitle;
    this.code = code;
    this.releaseYear = releaseYear;
    this.season = season;
    this.episode = episode;
  }

  public LocalInfo(Type type, String title) {
    this(null, type, title, null, null, null, null, null);
  }

  public Path getPath() {
    return path;
  }

  public Type getType() {
    return type;
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


}
