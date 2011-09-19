package hs.mediasystem.db;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieProvider implements ItemProvider {

  @Override
  public Item getItem(final Item item) {
    final String name = item.getTitle();
    
    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");
    
    List<Series> results = tvDB.searchSeries(name, "en");
    
    System.out.println("TVDB results:");
    System.out.println(results);
    
    final Series series = tvDB.getSeries(results.get(0).getId(), "en");
    
    try {
      return new Item(item.getPath()) {{
        byte[] banner = Downloader.readURL(new URL(series.getBanner()));
        byte[] poster = Downloader.readURL(new URL(series.getPoster()));
        byte[] background = Downloader.readURL(new URL(series.getFanart()));

        setLocalName(item.getPath().getFileName().toString());
        setTitle(name);
        setPlot(series.getOverview());
        setBanner(banner);
        setPoster(poster);
        setBackground(background);
        setProvider("TVDB");
        setProviderId(series.getId());
        setType("serie");
      }};
    }
    catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
