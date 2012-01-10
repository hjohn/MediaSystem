package hs.mediasystem.db;

import java.net.MalformedURLException;
import java.net.URL;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Episode;

public class TvdbEpisodeEnricher implements ItemEnricher {
//  private final String serieId;
//
//  public TvdbEpisodeEnricher(String serieId) {
//    this.serieId = serieId;
//  }

  @Override
  public void enrichItem(final Item item) throws ItemNotFoundException {
    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    String serieId = "";

    final Episode episode = tvDB.getEpisode(serieId, item.getSeason(), item.getEpisode(), "en");

    System.out.println("TvdbEpisodeProvider: episode = " + episode + "; serieId = " + serieId);

    try {
      byte[] poster = Downloader.readURL(new URL("http://thetvdb.com/banners/episodes/" + serieId + "/" + episode.getId() + ".jpg"));

      item.setProvider("TVDB");
      item.setProviderId(episode.getId());
      item.setPlot(episode.getOverview());
      item.setLocalName(item.getPath().getFileName().toString());
      item.setRating(Float.parseFloat(episode.getRating()));
      item.setTitle(item.getTitle());
      item.setSubtitle(episode.getEpisodeName());
      item.setSeason(item.getSeason());
      item.setEpisode(item.getEpisode());
      item.setPoster(poster);
      item.setType("episode");
    }
    catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
