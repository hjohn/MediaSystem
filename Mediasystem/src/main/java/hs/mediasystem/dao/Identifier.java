package hs.mediasystem.dao;

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

  private final String mediaType;
  private final String provider;
  private final String providerId;

  private MatchType matchType;
  private float matchAccuracy;

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

  public String getType() {
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

  @Override
  public String toString() {
    return "(" + mediaType + ";" + provider + ";" + providerId + ")";
  }
}
