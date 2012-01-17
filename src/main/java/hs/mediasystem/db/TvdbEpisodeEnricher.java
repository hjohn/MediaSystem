package hs.mediasystem.db;

import javax.inject.Inject;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Episode;

public class TvdbEpisodeEnricher implements ItemEnricher {
  private final ItemEnricher itemIdentifier;

  @Inject
  public TvdbEpisodeEnricher(ItemEnricher itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
  }

  @Override
  public void identifyItem(final Item item) throws ItemNotFoundException {
    Item serieItem = new Item() {{
      setTitle(item.getTitle());
      setType("SERIE");
    }};

    itemIdentifier.identifyItem(serieItem);

    item.setType("EPISODE");
    item.setProvider("TVDB");
    item.setProviderId(serieItem.getProviderId() + "," + item.getSeason() + "," + item.getEpisode());
  }

  @Override
  public void enrichItem(final Item item) throws ItemNotFoundException {
    if(item.getEpisode() != null) {
      System.out.println("[FINE] TvdbEpisodeEnricher: Enriching: " + item.getTitle() + " S" + item.getSeason() + "E" + item.getEpisode() + "; type=" + item.getType() + "; provider=" + item.getProvider() + "; providerId=" + item.getProviderId());
      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

      String[] split = item.getProviderId().split(",");

      final Episode episode = tvDB.getEpisode(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), "en");

      System.out.println("[FINE] TvdbEpisodeProvider: serieId = " + split[0] + ": Result: " + episode);

      byte[] poster = Downloader.tryReadURL("http://thetvdb.com/banners/episodes/" + split[0] + "/" + episode.getId() + ".jpg");

      item.setProvider("TVDB");
      item.setProviderId(episode.getId());
      item.setPlot(episode.getOverview());
      if(episode.getRating() != null) {
        item.setRating(Float.parseFloat(episode.getRating()));
      }
      item.setTitle(item.getTitle());
      item.setSubtitle(episode.getEpisodeName());
      item.setSeason(item.getSeason());
      item.setEpisode(item.getEpisode());
      item.setPoster(poster);
      item.setType("EPISODE");
    }
  }
}
