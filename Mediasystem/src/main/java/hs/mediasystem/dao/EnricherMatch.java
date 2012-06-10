package hs.mediasystem.dao;

import hs.mediasystem.dao.MediaData.MatchType;

public class EnricherMatch {
  private final Identifier identifier;
  private final MatchType matchType;
  private final float matchAccuracy;

  public EnricherMatch(Identifier identifier, MatchType matchType, float matchAccuracy) {
    assert identifier != null;
    assert matchType != null;
    assert matchAccuracy >= 0 && matchAccuracy <= 1.0;

    this.identifier = identifier;
    this.matchType = matchType;
    this.matchAccuracy = matchAccuracy;
  }

  public Identifier getIdentifier() {
    return identifier;
  }

  public MatchType getMatchType() {
    return matchType;
  }

  public float getMatchAccuracy() {
    return matchAccuracy;
  }
}
