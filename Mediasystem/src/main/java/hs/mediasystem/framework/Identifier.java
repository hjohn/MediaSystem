package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.entity.Entity;
import hs.mediasystem.entity.SimpleEntityProperty;
import javafx.beans.property.ObjectProperty;

public class Identifier extends Entity<Identifier> {
  public final ObjectProperty<ProviderId> providerId = object("providerId");
  public final ObjectProperty<MatchType> matchType = object("matchType");
  public final ObjectProperty<Float> matchAccuracy = object("matchAccuracy");

  public final SimpleEntityProperty<MediaData> mediaData = entity("mediaData");

  public Identifier(ProviderId providerId, MatchType matchType, Float matchAccuracy) {
    this.providerId.set(providerId);
    this.matchType.set(matchType);
    this.matchAccuracy.set(matchAccuracy);
  }
}
