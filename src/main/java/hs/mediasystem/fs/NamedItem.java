package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.ImageHandle;

import java.nio.file.Path;
import java.util.Date;

public abstract class NamedItem implements MediaItem {
  private final LocalInfo localInfo;

  protected MediaItem parent;

  private String plot = "";
  private Float rating;
  private Date releaseDate;
  private byte[] banner;
  private byte[] poster;
  private byte[] background;

  private boolean enriched;


  public NamedItem(LocalInfo localInfo) {
    this.localInfo = localInfo;
  }

  @Override
  public LocalInfo getLocalInfo() {
    return localInfo;
  }

  @Override
  public final String getTitle() {
    return localInfo.getTitle();
  }

  public MediaItem getParent() {
    return parent;
  }

  public Path getPath() {
    return localInfo.getPath();
  }

  @Override
  public String getUri() {
    return localInfo.getPath().toString();
  }

  @Override
  public String getSubtitle() {
    return localInfo.getSubtitle() == null ? "" : localInfo.getSubtitle();
  }

  @Override
  public Integer getReleaseYear() {
    return localInfo.getReleaseYear();
  }

  @Override
  public Integer getSeason() {
    return localInfo.getSeason();
  }

  @Override
  public Integer getEpisode() {
    return localInfo.getEpisode();
  }

  @Override
  public ImageHandle getBackground() {
    return background == null ? null : new ImageHandle(background, createKey("background"));
  }

  @Override
  public void setBackground(byte[] background) {
    this.background = background;
  }

  @Override
  public ImageHandle getBanner() {
    return banner == null ? null : new ImageHandle(banner, createKey("banner"));
  }

  @Override
  public void setBanner(byte[] banner) {
    this.banner = banner;
  }

  @Override
  public ImageHandle getPoster() {
    return poster == null ? null : new ImageHandle(poster, createKey("poster"));
  }

  @Override
  public void setPoster(byte[] poster) {
    this.poster = poster;
  }

  private String createKey(String suffix) {
    return getTitle() + "-" + getSeason() + "x" + getEpisode() + "-" + getSubtitle() + "-" + suffix;
  }

  @Override
  public String getPlot() {
    return plot;
  }

  @Override
  public void setPlot(String plot) {
    this.plot = plot;
  }

  @Override
  public Float getRating() {
    return rating;
  }

  @Override
  public void setRating(Float rating) {
    this.rating = rating;
  }

  @Override
  public Date getReleaseDate() {
    return releaseDate;
  }

  @Override
  public void setReleaseDate(Date date) {
    this.releaseDate = date;
  }

  @Override
  public boolean isEnriched() {
    return enriched;
  }

  @Override
  public void setEnriched(boolean enriched) {
    this.enriched = enriched;
  }

  @Override
  public String toString() {
    return "('" + localInfo.getTitle() + "', NamedItem[subtitle=" + localInfo.getSubtitle() + ", type=" + localInfo.getType() + "])";
  }
}
