package hs.mediasystem.db;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Episode;

public class TvdbEpisodeEnricher implements ItemEnricher {
  private final ItemIdentifier itemIdentifier;

  @Inject
  public TvdbEpisodeEnricher(ItemIdentifier itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
  }

  @Override
  public void enrichItem(final Item item) throws ItemNotFoundException {
    System.out.println("[FINE] TvdbEpisodeEnricher: Enriching: " + item.getTitle() + " S" + item.getSeason() + "E" + item.getEpisode());
    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    String serieId = itemIdentifier.identifyItem(new Item() {{
      setTitle(item.getTitle());
      setType("SERIE");
    }});

    final Episode episode = tvDB.getEpisode(serieId, item.getSeason(), item.getEpisode(), "en");

    System.out.println("[FINE] TvdbEpisodeProvider: serieId = " + serieId + ": Result: " + episode);

    try {
      byte[] poster = Downloader.readURL(new URL("http://thetvdb.com/banners/episodes/" + serieId + "/" + episode.getId() + ".jpg"));

      item.setProvider("TVDB");
      item.setProviderId(episode.getId());
      item.setPlot(episode.getOverview());
      item.setRating(Float.parseFloat(episode.getRating()));
      item.setTitle(item.getTitle());
      item.setSubtitle(episode.getEpisodeName());
      item.setSeason(item.getSeason());
      item.setEpisode(item.getEpisode());
      item.setPoster(poster);
      item.setType("EPISODE");
    }
    catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
