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
     * Matched manually by user
     */
    MANUAL,

    /**
     * Matched directly by a provider specific id (like an imdbid parsed from file name)
     */
    ID,

    /**
     * Matched by a hash calculated over the content of a file
     */
    HASH,

    /**
     * Matched on name and release year
     */
    NAME_AND_YEAR,

    /**
     * Matched on name only
     */
    NAME
  }

  @Id
  private Integer id;

  @Column
  private String mediaType;

  @Column
  private String provider;

  @Column
  private String providerId;

  @Column
  private MatchType matchType;

  @Column
  private float matchAccuracy;

  @Column(name = "mediadata_id")
  private Integer mediaDataId;

  @Column
  private Date lastUpdated;

  public Identifier(String mediaType, String provider, String providerId, MatchType matchType, float matchAccuracy) {
    assert mediaType != null;
    assert provider != null;
    assert providerId != null;
    assert matchType != null;
    assert matchAccuracy >= 0 && matchAccuracy <= 1.0;

    this.mediaType = mediaType;
    this.provider = provider;
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

  public String getMediaType() {
    return mediaType;
  }

  public String getProvider() {
    return provider;
  }

  public String getProviderId() {
    return providerId;
  }

  public MatchType getMatchType() {
    return matchType;
  }

  public float getMatchAccuracy() {
    return matchAccuracy;
  }

  public Integer getMediaDataId() {
    return mediaDataId;
  }

  public void setMediaDataId(Integer mediaDataId) {
    this.mediaDataId = mediaDataId;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Override
  public String toString() {
    return "(" + mediaType + ";" + provider + ";" + providerId + ")";
  }
}
