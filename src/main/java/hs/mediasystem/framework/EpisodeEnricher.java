package hs.mediasystem.framework;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.Source;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichTaskProvider;
import hs.mediasystem.enrich.Enricher;
import hs.mediasystem.enrich.TaskKey;
import hs.mediasystem.fs.SourceImageHandle;
import hs.mediasystem.media.Episode;
import hs.mediasystem.media.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class EpisodeEnricher implements Enricher<MediaItem, Episode> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
    add(MediaData.class);
    add(Media.class);
  }};

  private final ItemsDao itemsDao;
  private final TypeBasedItemEnricher typeBasedItemEnricher;

  @Inject
  public EpisodeEnricher(ItemsDao itemsDao, TypeBasedItemEnricher typeBasedItemEnricher) {
    this.itemsDao = itemsDao;
    this.typeBasedItemEnricher = typeBasedItemEnricher;
  }

  @Override
  public List<Class<?>> getInputTypes() {
    return INPUT_PARAMETERS;
  }

  @Override
  public EnrichTaskProvider<Episode> enrich(MediaItem key, Map<Class<?>, Object> inputParameters) {
    return new EpisodeEnrichTaskProvider(new TaskKey(key, Episode.class), (MediaData)inputParameters.get(MediaData.class), (Episode)inputParameters.get(Media.class));
  }

  private class EpisodeEnrichTaskProvider extends AbstractEnrichTaskProvider<Episode> {
    private final Episode currentEpisode;

    public EpisodeEnrichTaskProvider(TaskKey taskKey, MediaData mediaData, Episode currentEpisode) {
      super(itemsDao, typeBasedItemEnricher, taskKey, mediaData);
      this.currentEpisode = currentEpisode;
    }

    @Override
    public Episode itemToEnrichType(Item item) {
      Episode episode = new Episode(currentEpisode.getSerie(), item.getTitle(), currentEpisode.getSeason(), currentEpisode.getEpisode(), currentEpisode.getEndEpisode());

      episode.backgroundProperty().set(createImageHandle(item.getBackground(), item, "background"));
      episode.imageProperty().set(createImageHandle(item.getPoster(), item, "poster"));
      episode.descriptionProperty().set(item.getPlot());
      episode.ratingProperty().set(item.getRating());
      episode.runtimeProperty().set(item.getRuntime());
      episode.genresProperty().set(item.getGenres());
      episode.releaseDateProperty().set(item.getReleaseDate());

      return episode;
    }
  }

  private static SourceImageHandle createImageHandle(Source<byte[]> source, Item item, String keyPostFix) {
    String key = "EpisodeEnricher:/" + item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getSubtitle() + "-" + item.getImdbId() + "-" + keyPostFix;

    return source == null ? null : new SourceImageHandle(source, key);
  }
}
