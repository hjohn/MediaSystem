package hs.mediasystem.db;

import java.net.MalformedURLException;
import java.net.URL;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Episode;

public class TvdbEpisodeProvider implements ItemProvider {
  private final String serieId;

  public TvdbEpisodeProvider(String serieId) {
    this.serieId = serieId;
  }
  
  @Override
  public Item getItem(final Item item) throws ItemNotFoundException {
    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    final Episode episode = tvDB.getEpisode(serieId, item.getSeason(), item.getEpisode(), "en");
    
    System.out.println("TvdbEpisodeProvider: episode = " + episode + "; serieId = " + serieId);
    
    try {
      return new Item(item.getPath()) {{
        byte[] poster = Downloader.readURL(new URL("http://thetvdb.com/banners/episodes/" + serieId + "/" + episode.getId() + ".jpg"));
  
        setProvider("TVDB");
        setProviderId(episode.getId());
        setPlot(episode.getOverview());
        setLocalName(item.getPath().getFileName().toString());
        setRating(Float.parseFloat(episode.getRating()));
        setTitle(item.getTitle());
        setSubtitle(episode.getEpisodeName());
        setSeason(item.getSeason());
        setEpisode(item.getEpisode());
        setPoster(poster);
        setType("episode");
      }};
    }
    catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
