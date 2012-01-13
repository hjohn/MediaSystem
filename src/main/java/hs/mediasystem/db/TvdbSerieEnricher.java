package hs.mediasystem.db;

import java.util.List;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieEnricher implements ItemEnricher {
  private static final String TVDB = "TVDB";

  @Override
  public void identifyItem(final Item item) throws ItemNotFoundException {
    final String name = item.getTitle();

    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    List<Series> results = tvDB.searchSeries(name, "en");

    System.out.println("TVDB results:");
    System.out.println(results);

    if(results.isEmpty()) {
      throw new ItemNotFoundException(item);
    }
    
    item.setProvider(TVDB);
    item.setProviderId(results.get(0).getId());
    item.setType("SERIE");
  }

  @Override
  public void enrichItem(final Item item) {
    if(item.getProvider().equals(TVDB) && item.getType().equals("SERIE")) {
      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");
      
      final Series series = tvDB.getSeries(item.getProviderId(), "en");

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
      }
    }
    else {
      System.out.println("[FINE] TvdbSerieEnricher.enrichItem() - Unable to enrich, wrong provider or type: " + item);
    }
  }
}
