package hs.mediasystem.db;

public class MediaData {
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

  private int id;

  private String uri;

  private MediaId mediaId;
  private Identifier identifier;

  private boolean viewed;
  private int resumePosition;

  private MatchType matchType;
  private float matchAccuracy;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public boolean isViewed() {
    return viewed;
  }

  public void setViewed(boolean viewed) {
    this.viewed = viewed;
  }

  public int getResumePosition() {
    return resumePosition;
  }

  public void setResumePosition(int resumePosition) {
    this.resumePosition = resumePosition;
  }

  public float getMatchAccuracy() {
    return matchAccuracy;
  }

  public void setMatchAccuracy(float matchAccuracy) {
    this.matchAccuracy = matchAccuracy;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Identifier getIdentifier() {
    return identifier;
  }

  public void setIdentifier(Identifier identifier) {
    this.identifier = identifier;
  }

  public MediaId getMediaId() {
    return mediaId;
  }

  public void setMediaId(MediaId mediaId) {
    this.mediaId = mediaId;
  }

  public MatchType getMatchType() {
    return matchType;
  }

  public void setMatchType(MatchType matchType) {
    this.matchType = matchType;
  }
}
