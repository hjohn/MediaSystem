package hs.mediasystem.db;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieEnricher implements ItemEnricher {

  @Override
  public void enrichItem(final Item item) {
    final String name = item.getTitle();

    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    List<Series> results = tvDB.searchSeries(name, "en");

    System.out.println("TVDB results:");
    System.out.println(results);

    if(!results.isEmpty()) {
      final Series series = tvDB.getSeries(results.get(0).getId(), "en");

      try {
        byte[] banner = Downloader.readURL(new URL(series.getBanner()));
        byte[] poster = Downloader.readURL(new URL(series.getPoster()));
        byte[] background = Downloader.readURL(new URL(series.getFanart()));

        item.setTitle(name);
        item.setPlot(series.getOverview());
        item.setBanner(banner);
        item.setPoster(poster);
        item.setBackground(background);
        item.setProvider("TVDB");
        item.setProviderId(series.getId());
        item.setType("SERIE");
      }
      catch(MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    else {
      System.out.println("[INFO] " + getClass().getSimpleName() + ": Could not find: " + name);
    }
  }
}
