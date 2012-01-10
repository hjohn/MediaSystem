package hs.mediasystem.db;

import java.nio.file.Path;

public class LocalItem extends Item {
  private final Path path;

  private String localTitle;
  private String localSubtitle;
  private String localReleaseYear;

  public LocalItem(Path path) {
    this.path = path;
  }

  public String getLocalTitle() {
    return localTitle;
  }

  public void setLocalTitle(String localTitle) {
    this.localTitle = localTitle;
  }

  public String getLocalSubtitle() {
    return localSubtitle;
  }

  public void setLocalSubtitle(String localSubtitle) {
    this.localSubtitle = localSubtitle;
  }

  public String getLocalReleaseYear() {
    return localReleaseYear;
  }

  public void setLocalReleaseYear(String localReleaseYear) {
    this.localReleaseYear = localReleaseYear;
  }

  public Path getPath() {
    return path;
  }

  public void calculateSurrogateName() {
    String subtitle = getLocalSubtitle() != null ? getLocalSubtitle().toLowerCase() : "";

    surrogateName = getType() + "/" + getLocalTitle().toLowerCase() + "/" + subtitle + "/" + getSeason() + "/" + getEpisode() + "/" + getLocalReleaseYear();
  }
}
