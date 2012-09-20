package hs.mediasystem.framework;

import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.dao.Source;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.EntityProvider;
import hs.mediasystem.entity.FinishEnrichCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;

public abstract class MediaProvider<T extends Media<T>> implements EntityProvider<Item, T> {

  @Override
  public final T get(final Item item) {
    final T media = createMedia(item);

    if(media == null) {
      return media;
    }

    media.title.set(item.getTitle());
    media.background.set(createImageHandle(item.getBackground(), item, "background"));
    media.banner.set(createImageHandle(item.getBanner(), item, "banner"));
    media.image.set(createImageHandle(item.getPoster(), item, "poster"));
    media.description.set(item.getPlot());
    media.rating.set(item.getRating());
    media.runtime.set(item.getRuntime());
    media.genres.set(item.getGenres());
    media.releaseDate.set(item.getReleaseDate());

    media.castings.setEnricher(new EnricherBuilder<T, List<Casting>>(List.class)
      .enrich(new EnrichCallback<List<Casting>>() {
        @Override
        public List<Casting> enrich(Object... parameters) {
          List<hs.mediasystem.dao.Casting> castings = item.getCastings();

          Collections.sort(castings, Item.CASTINGS_BY_INDEX_THEN_CHARACTERNAME);

          List<Casting> result = new ArrayList<>();

          for(final hs.mediasystem.dao.Casting casting : castings) {
            Person p = media.create(Person.class, casting.getPerson());

            Casting c = new Casting();

            c.media.set(media);
            c.person.set(p);
            c.characterName.set(casting.getCharacterName());
            c.index.set(casting.getIndex());
            c.role.set(casting.getRole());

            result.add(c);
          }

          return result;
        }
      })
      .finish(new FinishEnrichCallback<List<Casting>>() {
        @Override
        public void update(List<Casting> result) {
          media.castings.set(FXCollections.observableList(result));
        }
      })
      .build()
    );

    media.identifiers.setEnricher(new EnricherBuilder<T, List<Identifier>>(List.class)
      .enrich(new EnrichCallback<List<Identifier>>() {
        @Override
        public List<Identifier> enrich(Object... parameters) {
          List<Identifier> results = new ArrayList<>();

          try(Transaction transaction = item.getDatabase().beginTransaction()) {
            ProviderId providerId = item.getProviderId();

            for(hs.mediasystem.dao.Identifier identifier : transaction.select(hs.mediasystem.dao.Identifier.class, "mediatype=? AND provider=? AND providerid=?", providerId.getType(), providerId.getProvider(), providerId.getId())) {
              Identifier i = media.create(Identifier.class, identifier);

              i.mediaData.set(media.create(MediaData.class, identifier.getMediaData()));

              results.add(i);
            }
          }

          return results;
        }
      })
      .finish(new FinishEnrichCallback<List<Identifier>>() {
        @Override
        public void update(List<Identifier> result) {
          media.identifiers.set(FXCollections.observableList(result));
        }
      })
      .build()
    );

    configureMedia(media, item);

    return media;
  }

  protected abstract T createMedia(Item item);
  protected abstract void configureMedia(T media, Item item);

  private static SourceImageHandle createImageHandle(Source<byte[]> source, Item item, String keyPostFix) {
    String key = "Media:/" + item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getImdbId() + "-" + keyPostFix;

    return source == null ? null : new SourceImageHandle(source, key);
  }
}