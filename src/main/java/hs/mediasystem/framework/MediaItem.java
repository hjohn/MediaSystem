package hs.mediasystem.framework;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.util.ImageHandle;

import java.util.Date;

public interface MediaItem {
  MediaTree getRoot();
  boolean isRoot();
  boolean isLeaf();

  String getUri();

  String getTitle();
  String getSubtitle();
  Integer getReleaseYear();
  Integer getSeason();
  Integer getEpisode();

  String getPlot();
  Float getRating();
  Date getReleaseDate();
  String[] getGenres();
  int getRuntime();
  String getTagline();
  String getLanguage();
  ImageHandle getBanner();
  ImageHandle getPoster();
  ImageHandle getBackground();

  void setTitle(String title);
  void setPlot(String plot);
  void setRating(Float rating);
  void setReleaseDate(Date date);
  void setLanguage(String language);
  void setTagline(String tagline);
  void setRuntime(int minutes);
  void setGenres(String[] genres);
  void setBanner(byte[] banner);
  void setPoster(byte[] poster);
  void setBackground(byte[] background);

  LocalInfo getLocalInfo();
  void setEnriched(boolean enriched);
  boolean isEnriched();
}
