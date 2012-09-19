package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.Entity;
import hs.mediasystem.entity.FinishEnrichCallback;
import hs.mediasystem.entity.SimpleEntityProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Identifier extends Entity<Identifier> {
  public final ObjectProperty<hs.mediasystem.dao.Identifier> dbIdentifier = object("dbIdentifier");

  public final ObjectProperty<ProviderId> providerId = object("providerId");
  public final ObjectProperty<MatchType> matchType = object("matchType");
  public final ObjectProperty<Float> matchAccuracy = object("matchAccuracy");

  public final SimpleEntityProperty<MediaData> mediaData = entity("mediaData");

  public Identifier(hs.mediasystem.dao.Identifier dbIdentifier) {
    assert dbIdentifier != null;

    this.dbIdentifier.addListener(new ChangeListener<hs.mediasystem.dao.Identifier>() {
      @Override
      public void changed(ObservableValue<? extends hs.mediasystem.dao.Identifier> observableValue, hs.mediasystem.dao.Identifier old, hs.mediasystem.dao.Identifier current) {
        providerId.set(current.getProviderId());
        matchAccuracy.set(current.getMatchAccuracy());
        matchType.set(current.getMatchType());
      }
    });

    mediaData.setEnricher(new EnricherBuilder<Identifier, MediaData>(MediaData.class)
      .require(this.dbIdentifier)
      .enrich(new EnrichCallback<MediaData>() {
        @Override
        public MediaData enrich(Object... parameters) {
          System.out.println(">>> MediaData enricher was triggers for " + this);
          hs.mediasystem.dao.Identifier dbIdentifier = (hs.mediasystem.dao.Identifier)parameters[0];

          return new MediaData(dbIdentifier.getMediaData());
        }
      })
      .finish(new FinishEnrichCallback<MediaData>() {
        @Override
        public void update(MediaData result) {
          mediaData.set(result);
        }
      })
      .build()
    );

    this.dbIdentifier.set(dbIdentifier);
  }
}
