package hs.mediasystem.ext.serie;

import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.Source;
import hs.mediasystem.dao.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichTask;
import hs.mediasystem.enrich.Enricher;
import hs.mediasystem.enrich.Parameters;
import hs.mediasystem.framework.AbstractEnrichTaskProvider;
import hs.mediasystem.framework.Episode;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.TaskTitle;
import hs.mediasystem.fs.SourceImageHandle;
import hs.mediasystem.util.ImageHandle;

import java.util.ArrayList;
import java.util.List;

public class EpisodeEnricher implements Enricher<Episode> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
    add(TaskTitle.class);
    add(MediaData.class);
    add(Media.class);
  }};

  private volatile ItemsDao itemsDao;
  private volatile TypeBasedItemEnricher typeBasedItemEnricher;

  @Override
  public List<Class<?>> getInputTypes() {
    return INPUT_PARAMETERS;
  }

  @Override
  public List<EnrichTask<Episode>> enrich(Parameters parameters, boolean bypassCache) {
    List<EnrichTask<Episode>> enrichTasks = new ArrayList<>();

    EpisodeEnrichTaskProvider enrichTaskProvider = new EpisodeEnrichTaskProvider(parameters.unwrap(TaskTitle.class), parameters.get(MediaData.class), (Episode)parameters.get(Media.class));

    if(!bypassCache) {
      enrichTasks.add(enrichTaskProvider.getCachedTask());
    }
    enrichTasks.add(enrichTaskProvider.getTask(bypassCache));

    return enrichTasks;
  }

  private class EpisodeEnrichTaskProvider extends AbstractEnrichTaskProvider<Episode> {
    private final Episode currentEpisode;

    public EpisodeEnrichTaskProvider(String title, MediaData mediaData, Episode currentEpisode) {
      super(title, itemsDao, typeBasedItemEnricher, mediaData);
      this.currentEpisode = currentEpisode;
    }

    @Override
    public Episode itemToEnrichType(Item item) {
      Episode episode = new Episode(currentEpisode.getSerie(), item.getTitle(), currentEpisode.getSeason(), currentEpisode.getEpisode(), currentEpisode.getEndEpisode());

      ImageHandle backgroundHandle = createImageHandle(item.getBackground(), item, "background");

      episode.backgroundProperty().set(backgroundHandle == null ? currentEpisode.getSerie().getMedia().getBackground() : backgroundHandle);
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
    String key = "EpisodeEnricher:/" + item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getImdbId() + "-" + keyPostFix;

    return source == null ? null : new SourceImageHandle(source, key);
  }
}
