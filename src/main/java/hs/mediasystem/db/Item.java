package hs.mediasystem.db;

import java.util.Date;

public class Item {
  private int id;
  private String localName;
  private String title;
  private String imdbId;
  private String tmdbId;
  private String plot;
  private byte[] cover;
  private byte[] background;
  private float rating;
  private Date releaseDate;
  private int runtime;
  private int version;
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getLocalName() {
    return localName;
  }
  
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getImdbId() {
    return imdbId;
  }
  
  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }
  
  public String getTmdbId() {
    return tmdbId;
  }
  
  public void setTmdbId(String tmdbId) {
    this.tmdbId = tmdbId;
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
  
  public int getVersion() {
    return version;
  }
  
  public void setVersion(int version) {
    this.version = version;
  }
}
