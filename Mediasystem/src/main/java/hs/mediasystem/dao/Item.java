package hs.mediasystem.dao;

import hs.mediasystem.db.AnnotatedRecordMapper;
import hs.mediasystem.db.Column;
import hs.mediasystem.db.DataTypeConverter;
import hs.mediasystem.db.Database;
import hs.mediasystem.db.Id;
import hs.mediasystem.db.Table;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Table(name = "items")
public class Item {
  public static final int VERSION = 2;

  @Id
  private Integer id;

  @Column(name = {"type", "provider", "providerid"})
  private ProviderId providerId;

  @Column
  private String title;

  @Column(converterClass = VersionConverter.class)
  private int version;

  @Column(converterClass = UpdateDateConverter.class)
  private Date lastChecked;

  @Column(converterClass = UpdateDateConverter.class)
  private Date lastHit;

  @Column(converterClass = UpdateDateConverter.class)
  private Date lastUpdated;

  @Column
  private String imdbId;

  @Column
  private String plot;

  @Column
  private String backgroundURL;

  @Column
  private String bannerURL;

  @Column
  private String posterURL;

  @Column
  private Float rating;

  @Column
  private Date releaseDate;

  @Column
  private int runtime;

  @Column
  private Integer season;

  @Column
  private Integer episode;

  @Column(converterClass = GenresConverter.class)
  private String[] genres = new String[] {};

  @Column
  private String language;

  @Column
  private String tagline;

  private Source<byte[]> background;
  private Source<byte[]> banner;
  private Source<byte[]> poster;
  private List<Casting> castings;

  public Item(ProviderId providerId) {
    this.providerId = providerId;
  }

  public Item() {
  }

  public void afterLoadStore(Database database) throws SQLException {
    setBackground(DatabaseUrlSource.create(database, getBackgroundURL()));
    setBanner(DatabaseUrlSource.create(database, getBannerURL()));
    setPoster(DatabaseUrlSource.create(database, getPosterURL()));
  }

  public ProviderId getProviderId() {
    return providerId;
  }

  public void setProviderId(ProviderId providerId) {
    this.providerId = providerId;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
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

  public Source<byte[]> getPoster() {
    return poster;
  }

  public void setPoster(Source<byte[]> poster) {
    this.poster = poster;
  }

  public Source<byte[]> getBackground() {
    return background;
  }

  public void setBackground(Source<byte[]> background) {
    this.background = background;
  }

  public Source<byte[]> getBanner() {
    return banner;
  }

  public void setBanner(Source<byte[]> banner) {
    this.banner = banner;
  }

  public Float getRating() {
    return rating;
  }

  public void setRating(Float rating) {
    this.rating = rating;
  }

  public Date getReleaseDate() {
    return releaseDate == null ? null : (Date)releaseDate.clone();
  }

  public void setReleaseDate(Date releaseDate) {
    this.releaseDate = releaseDate == null ? null : (Date)releaseDate.clone();
  }

  public int getRuntime() {
    return runtime;
  }

  public void setRuntime(int runtime) {
    this.runtime = runtime;
  }

  public Integer getSeason() {
    return season;
  }

  public void setSeason(Integer season) {
    this.season = season;
  }

  public Integer getEpisode() {
    return episode;
  }

  public void setEpisode(Integer episode) {
    this.episode = episode;
  }

  public String[] getGenres() {
    return genres.clone();
  }

  public void setGenres(String[] genres) {
    this.genres = genres == null ? new String[] {} : genres;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getTagline() {
    return tagline;
  }

  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  public String getBackgroundURL() {
    return backgroundURL;
  }

  public void setBackgroundURL(String backgroundURL) {
    this.backgroundURL = backgroundURL;
  }

  public String getBannerURL() {
    return bannerURL;
  }

  public void setBannerURL(String bannerURL) {
    this.bannerURL = bannerURL;
  }

  public String getPosterURL() {
    return posterURL;
  }

  public void setPosterURL(String posterURL) {
    this.posterURL = posterURL;
  }

  public Date getLastChecked() {
    return lastChecked;
  }

  public void setLastChecked(Date lastChecked) {
    this.lastChecked = lastChecked;
  }

  public Date getLastHit() {
    return lastHit;
  }

  public void setLastHit(Date lastHit) {
    this.lastHit = lastHit;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public boolean isCastingsLoaded() {
    return castings != null;
  }

  public List<Casting> getCastings() {
    if(castings == null) {
      castings = AnnotatedRecordMapper.fetch(Casting.class, this);
    }
    return castings;
  }

  @Override
  public String toString() {
    return "('" + title + "', Item[id=" + id + ", providerId=" + providerId + "])";
  }

  public static class UpdateDateConverter implements DataTypeConverter<Date, Date> {
    @Override
    public Date toStorageType(Date input) {
      return new Date();
    }

    @Override
    public Date toJavaType(Date input, Class<? extends Date> type) {
      return input;
    }
  }

  public static class VersionConverter implements DataTypeConverter<Integer, Integer> {
    @Override
    public Integer toStorageType(Integer input) {
      return VERSION;
    }

    @Override
    public Integer toJavaType(Integer input, Class<? extends Integer> type) {
      return input;
    }
  }

  public static class GenresConverter implements DataTypeConverter<String[], String> {
    @Override
    public String toStorageType(String[] input) {
      String genres = "";

      for(String genre : input) {
        if(genre.length() > 0) {
          genres += ",";
        }
        genres += genre;
      }

      return genres;
    }

    @Override
    public String[] toJavaType(String input, Class<? extends String[]> type) {
      return input == null ? new String[] {} : input.split(",");
    }
  }
}
