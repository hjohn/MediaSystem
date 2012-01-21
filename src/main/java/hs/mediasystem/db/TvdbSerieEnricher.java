package hs.mediasystem.db;

import java.util.List;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieEnricher implements ItemEnricher {
  private static final String TVDB = "TVDB";

  @Override
  public Identifier identifyItem(final LocalInfo localInfo) throws IdentifyException {
    final String name = localInfo.getTitle();

    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    List<Series> results = tvDB.searchSeries(name, "en");

    System.out.println("TVDB results:");
    System.out.println(results);

    if(results.isEmpty()) {
      throw new IdentifyException(localInfo);
    }

    return new Identifier(MediaType.SERIE, TVDB, results.get(0).getId());
  }

  @Override
  public Item loadItem(Identifier identifier) throws ItemNotFoundException {
    if(identifier.getType() != MediaType.SERIE || !identifier.getProvider().equals(TVDB)) {
      throw new RuntimeException("Unable to enrich, wrong provider or type: " + identifier);
    }

    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    final Series series = tvDB.getSeries(identifier.getProviderId(), "en");

    if(series == null) {
      throw new ItemNotFoundException(identifier);
    }

    byte[] banner = Downloader.tryReadURL(series.getBanner());
    byte[] poster = Downloader.tryReadURL(series.getPoster());
    byte[] background = Downloader.tryReadURL(series.getFanart());

    Item item = new Item(identifier);

    item.setTitle(series.getSeriesName());
    item.setPlot(series.getOverview());
    item.setRating(Float.valueOf(series.getRating()));
    item.setBanner(banner);
    item.setPoster(poster);
    item.setBackground(background);

    return item;
  }
}
