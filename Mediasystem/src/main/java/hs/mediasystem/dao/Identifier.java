package hs.mediasystem.dao;

import hs.mediasystem.db.Column;
import hs.mediasystem.db.Id;
import hs.mediasystem.db.Table;

import java.util.Date;

@Table(name = "identifiers")
public class Identifier {
  /**
   * MatchType enum, in order of certainty
   */
  public enum MatchType {
    /**
     * Matched manually by user.
     */
    MANUAL,

    /**
     * Matched directly by a provider specific id (like an imdbid parsed from file name).
     */
    ID,

    /**
     * Matched by a hash calculated over the content of a file.
     */
    HASH,

    /**
     * Matched on name and release year.
     */
    NAME_AND_YEAR,

    /**
     * Matched on name only.
     */
    NAME
  }

  @Id
  private Integer id;

  @Column(name = {"mediatype", "provider", "providerid"})
  private ProviderId providerId;

  @Column
  private MatchType matchType;

  @Column
  private Float matchAccuracy;

  @Column(name = "mediadata_id")
  private MediaData mediaData;

  @Column
  private Date lastUpdated;

  public Identifier(ProviderId providerId, MatchType matchType, float matchAccuracy) {
    assert providerId != null;
    assert matchType != null;
    assert matchAccuracy >= 0 && matchAccuracy <= 1.0;

    this.providerId = providerId;
    this.matchType = matchType;
    this.matchAccuracy = matchAccuracy;
  }

  public Identifier() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public ProviderId getProviderId() {
    return providerId;
  }

  public MatchType getMatchType() {
    return matchType;
  }

  public Float getMatchAccuracy() {
    return matchAccuracy;
  }

  public MediaData getMediaData() {
    return mediaData;
  }

  public void setMediaData(MediaData mediaData) {
    this.mediaData = mediaData;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public String toString() {
    return "Identifier(" + providerId + "; matchType: " + matchType + ")";
  }
}
