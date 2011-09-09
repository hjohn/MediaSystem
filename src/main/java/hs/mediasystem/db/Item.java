package hs.mediasystem.db;

import java.nio.file.Path;
import java.util.Date;

public class Item extends AbstractRecord {
  private String localName;
  private String imdbId;
  private String plot;
  private byte[] cover;
  private byte[] background;
  private float rating;
  private Date releaseDate;
  private int runtime;
  private int season;
  private int episode;
  private String subtitle;
  
  private final Path path;
  
  public Item(Path path) {
    this.path = path;
  }
  
  public Path getPath() {
    return path;
  }
  
  public String getLocalName() {
    return localName;
  }
  
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  
  public String getImdbId() {
    return imdbId;
  }
  
  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }
  
  public String getPlot() {
    return plot;
  }
  
  public void setPlot(String plot) {
    this.plot = plot;
  }
  
  public byte[] getCover() {
    return cover;
  }
  
  public void setCover(byte[] cover) {
    this.cover = cover;
  }
  
  public byte[] getBackground() {
    return background;
  }
  
  public void setBackground(byte[] background) {
    this.background = background;
  }
  
  public float getRating() {
    return rating;
  }

  public void setRating(float rating) {
    this.rating = rating;
  }
  
  public Date getReleaseDate() {
    return releaseDate;
  }
  
  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate;
  }

  public int getRuntime() {
    return runtime;
  }
  
  public void setRuntime(int runtime) {
    this.runtime = runtime;
  }

  public int getSeason() {
    return season;
  }
  
  public void setSeason(int season) {
    this.season = season;
  }
  
  public int getEpisode() {
    return episode;
  }
  
  public void setEpisode(int episode) {
    this.episode = episode;
  }
  
  public String getSubtitle() {
    return subtitle;
  }
  
  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }
}
