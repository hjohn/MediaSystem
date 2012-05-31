package hs.mediasystem.framework;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.Source;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichTask;
import hs.mediasystem.enrich.Enricher;
import hs.mediasystem.enrich.Parameters;
import hs.mediasystem.fs.SourceImageHandle;
import hs.mediasystem.media.Serie;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SerieEnricher implements Enricher<Serie> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
    add(TaskTitle.class);
    add(MediaData.class);
  }};

  private final ItemsDao itemsDao;
  private final TypeBasedItemEnricher typeBasedItemEnricher;

  @Inject
  public SerieEnricher(ItemsDao itemsDao, TypeBasedItemEnricher typeBasedItemEnricher) {
    this.itemsDao = itemsDao;
    this.typeBasedItemEnricher = typeBasedItemEnricher;
  }

  @Override
  public List<Class<?>> getInputTypes() {
    return INPUT_PARAMETERS;
  }

  @Override
  public List<EnrichTask<Serie>> enrich(Parameters parameters, boolean bypassCache) {
    List<EnrichTask<Serie>> enrichTasks = new ArrayList<>();

    SerieEnrichTaskProvider enrichTaskProvider = new SerieEnrichTaskProvider(parameters.unwrap(TaskTitle.class), parameters.get(MediaData.class));

    if(!bypassCache) {
      enrichTasks.add(enrichTaskProvider.getCachedTask());
    }
    enrichTasks.add(enrichTaskProvider.getTask(bypassCache));

    return enrichTasks;
  }

  private class SerieEnrichTaskProvider extends AbstractEnrichTaskProvider<Serie> {
    public SerieEnrichTaskProvider(String title, MediaData mediaData) {
      super(title, itemsDao, typeBasedItemEnricher, mediaData);
    }

    @Override
    public Serie itemToEnrichType(Item item) {
      Serie serie = new Serie(item.getTitle());

      serie.backgroundProperty().set(createImageHandle(item.getBackground(), item, "background"));
      serie.bannerProperty().set(createImageHandle(item.getBanner(), item, "banner"));
      serie.imageProperty().set(createImageHandle(item.getPoster(), item, "poster"));
      serie.descriptionProperty().set(item.getPlot());
      serie.ratingProperty().set(item.getRating());
      serie.runtimeProperty().set(item.getRuntime());
      serie.genresProperty().set(item.getGenres());
      serie.releaseDateProperty().set(item.getReleaseDate());

      return serie;
    }
  }

  private static SourceImageHandle createImageHandle(Source<byte[]> source, Item item, String keyPostFix) {
    String key = "SerieEnricher:/" + item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getSubtitle() + "-" + item.getImdbId() + "-" + keyPostFix;

    return source == null ? null : new SourceImageHandle(source, key);
  }
}
