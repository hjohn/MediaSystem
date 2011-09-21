package hs.mediasystem.db;

import java.nio.file.Path;
import java.util.Date;

public class Item extends AbstractRecord {
  private String localName;
  private String imdbId;
  private String plot;
  private byte[] poster;
  private byte[] background;
  private byte[] banner;
  private Float rating;
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
  
  public byte[] getPoster() {
    return poster;
  }
  
  public void setPoster(byte[] poster) {
    this.poster = poster;
  }
  
  public byte[] getBackground() {
    return background;
  }
  
  public void setBackground(byte[] background) {
    this.background = background;
  }
  
  public Float getRating() {
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
  
  public byte[] getBanner() {
    return banner;
  }
  
  public void setBanner(byte[] banner) {
    this.banner = banner;
  }
}
