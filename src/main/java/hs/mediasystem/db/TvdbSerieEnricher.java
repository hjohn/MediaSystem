package hs.mediasystem.db;

import java.util.List;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieEnricher implements ItemEnricher {
  private static final String TVDB = "TVDB";

  @Override
  public Identifier identifyItem(final Item item) throws ItemNotFoundException {
    final String name = item.getTitle();

    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    List<Series> results = tvDB.searchSeries(name, "en");

    System.out.println("TVDB results:");
    System.out.println(results);

    if(results.isEmpty()) {
      throw new ItemNotFoundException(item);
    }

    return new Identifier("SERIE", TVDB, results.get(0).getId());
  }

  @Override
  public Item enrichItem(final Item item, Identifier identifier) throws ItemNotFoundException {
    if(identifier.getType().equals("SERIE") && identifier.getProvider().equals(TVDB)) {
      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

      final Series series = tvDB.getSeries(identifier.getProviderId(), "en");

      if(series != null) {
        byte[] banner = Downloader.tryReadURL(series.getBanner());
        byte[] poster = Downloader.tryReadURL(series.getPoster());
        byte[] background = Downloader.tryReadURL(series.getFanart());

        item.setTitle(series.getSeriesName());
        item.setPlot(series.getOverview());
        item.setRating(Float.valueOf(series.getRating()));
        item.setBanner(banner);
        item.setPoster(poster);
        item.setBackground(background);

        item.setType(identifier.getType());
        item.setProvider(identifier.getProvider());
        item.setProviderId(identifier.getProviderId());

        return item;
      }
      else {
        throw new ItemNotFoundException(item);
      }
    }
    else {
      throw new RuntimeException("Unable to enrich, wrong provider or type: " + identifier);
    }
  }
}
