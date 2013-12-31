package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.entity.Entity;
import javafx.beans.property.ObjectProperty;

public class Identifier extends Entity {
  public final ObjectProperty<ProviderId> providerId = object("providerId");
  public final ObjectProperty<MatchType> matchType = object("matchType");
  public final ObjectProperty<Float> matchAccuracy = object("matchAccuracy");

  public final ObjectProperty<MediaData> mediaData = object("mediaData");

  public Identifier setAll(ProviderId providerId, MatchType matchType, Float matchAccuracy) {
    this.providerId.set(providerId);
    this.matchType.set(matchType);
    this.matchAccuracy.set(matchAccuracy);

    return this;
  }
}
