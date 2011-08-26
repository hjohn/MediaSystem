package hs.mediasystem.screens.movie;

import java.nio.file.Path;

public class Element {
  private final Path path;
  private final String title;
  private final String sequence;
  private final String subtitle;
  private final String year;
  private final String imdbNumber;

  public Element(Path path, String title, String subtitle, String sequence, String year, String imdbNumber) {
    this.path = path;
    this.title = title;
    this.subtitle = subtitle;
    this.sequence = sequence;
    this.year = year;
    this.imdbNumber = imdbNumber;
  }

  public Path getPath() {
    return path;
  }
  
  public String getTitle() {
    return title;
  }
  
  public String getSubtitle() {
    return subtitle;
  }
  
  public String getYear() {
    return year;
  }
  
  public String getSequence() {
    return sequence;
  }
}

