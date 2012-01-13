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
    itemIdentifier.identifyItem(new Item() {{
      setTitle(item.getTitle());
      setType("SERIE");
    }});

    item.setType("EPISODE");
    item.setProvider("TVDB");
    item.setProviderId(item.getProviderId() + "," + item.getSeason() + "," + item.getEpisode());
  }

  @Override
  public void enrichItem(final Item item) throws ItemNotFoundException {
    System.out.println("[FINE] TvdbEpisodeEnricher: Enriching: " + item.getTitle() + " S" + item.getSeason() + "E" + item.getEpisode());
    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    String[] split = item.getProviderId().split(",");

    final Episode episode = tvDB.getEpisode(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), "en");

    System.out.println("[FINE] TvdbEpisodeProvider: serieId = " + split[0] + ": Result: " + episode);

    byte[] poster = Downloader.tryReadURL("http://thetvdb.com/banners/episodes/" + split[0] + "/" + episode.getId() + ".jpg");

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
}
