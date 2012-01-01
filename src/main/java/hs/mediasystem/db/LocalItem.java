package hs.mediasystem.db;

import java.nio.file.Path;

public class LocalItem extends Item {
  private String localTitle;
  private String localSubtitle;
  private String localReleaseYear;

  public LocalItem(Path path) {
    super(path);
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
}
