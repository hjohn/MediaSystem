package hs.mediasystem.db;

public class LocalInfo {
  private final String uri;
  private final String groupName;
  private final String title;
  private final String subtitle;
  private final String code;
  private final Integer releaseYear;
  private final Integer season;
  private final Integer episode;
  private final Integer endEpisode;

  public LocalInfo(String uri, String groupName, String title, String subtitle, String code, Integer releaseYear, Integer season, Integer episode, Integer endEpisode) {
    assert uri != null;
    assert title == null || !title.isEmpty();
    assert subtitle == null || !subtitle.isEmpty();
    assert code == null || !code.isEmpty();

    this.uri = uri;
    this.groupName = groupName;
    this.title = title;
    this.subtitle = subtitle;
    this.code = code;
    this.releaseYear = releaseYear;
    this.season = season;
    this.episode = episode;
    this.endEpisode = endEpisode;
  }

  public LocalInfo(String uri, String title, Integer releaseYear) {
    this(uri, null, title, null, null, releaseYear, null, null, null);
  }

  public LocalInfo(String uri, String title) {
    this(uri, null, title, null, null, null, null, null, null);
  }

  public String getUri() {
    return uri;
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

  public Integer getEndEpisode() {
    return endEpisode;
  }

  @Override
  public int hashCode() {
    return uri.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }
    return uri.equals(((LocalInfo)obj).uri);
  }

  @Override
  public String toString() {
    return "LocalInfo('" + title + "', season=" + season + ", ep=" + episode + ", uri=" + uri + ")";
  }
}
