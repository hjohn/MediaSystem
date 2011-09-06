package hs.mediasystem.db;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieProvider implements SerieProvider {

  @Override
  public SerieRecord getSerie(final String name) {
    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");
    
    List<Series> results = tvDB.searchSeries(name, "en");
    
    System.out.println("TVDB results:");
    System.out.println(results);
    
    final Series series = tvDB.getSeries(results.get(0).getId(), "en");
    
    try {
      return new SerieRecord() {{
        byte[] poster = Downloader.readURL(new URL(series.getBanner()));
        
        setTitle(name);
        setOverview(series.getOverview());
        setBanner(poster);
      }};
    }
    catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
