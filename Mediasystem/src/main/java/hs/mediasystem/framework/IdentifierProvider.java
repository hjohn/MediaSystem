package hs.mediasystem.framework;

import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.EntityProvider;
import hs.mediasystem.entity.FinishEnrichCallback;

public class IdentifierProvider implements EntityProvider<hs.mediasystem.dao.Identifier, Identifier> {
  @Override
  public Identifier get(final hs.mediasystem.dao.Identifier dbIdentifier) {
    final Identifier identifier = new Identifier(
      dbIdentifier.getProviderId(),
      dbIdentifier.getMatchType(),
      dbIdentifier.getMatchAccuracy()
    );

    identifier.mediaData.setEnricher(new EnricherBuilder<Identifier, MediaData>(MediaData.class)
      .enrich(new EnrichCallback<MediaData>() {
        @Override
        public MediaData enrich(Object... parameters) {
          return identifier.create(MediaData.class, dbIdentifier.getMediaData());
        }
      })
      .finish(new FinishEnrichCallback<MediaData>() {
        @Override
        public void update(MediaData result) {
          identifier.mediaData.set(result);
        }
      })
      .build()
    );

    return identifier;
  }

  @Override
  public Class<?> getType() {
    return Identifier.class;
  }
}