package hs.mediasystem.db;

import java.util.List;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieIdentifier implements ItemIdentifier {

  @Override
  public String identifyItem(Item item) {
    final String name = item.getTitle();

    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    List<Series> results = tvDB.searchSeries(name, "en");

    System.out.println("TVDB results:");
    System.out.println(results);

    if(!results.isEmpty()) {
      return results.get(0).getId();
    }

    return null;
  }
}
