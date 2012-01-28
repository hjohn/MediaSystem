package hs.mediasystem.db;

import javax.inject.Inject;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Episode;

public class TvdbEpisodeEnricher implements ItemEnricher {
  private final TvdbSerieEnricher itemIdentifier;

  @Inject
  public TvdbEpisodeEnricher(TvdbSerieEnricher itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
  }

  @Override
  public String getProviderCode() {
    return "TVDB";
  }

  @Override
  public String identifyItem(final LocalInfo localInfo) throws IdentifyException {
    String identifier = itemIdentifier.identifyItem(new LocalInfo(MediaType.SERIE, localInfo.getTitle()));

    return identifier + "," + localInfo.getSeason() + "," + (localInfo.getEpisode() == null ? 0 : localInfo.getEpisode());
  }

  @Override
  public Item loadItem(String identifier) throws ItemNotFoundException {
    TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

    String[] split = identifier.split(",");
    int seasonNumber = Integer.parseInt(split[1]);
    int episodeNumber = Integer.parseInt(split[2]);

    final Episode episode = tvDB.getEpisode(split[0], seasonNumber, episodeNumber, "en");

    System.out.println("[FINE] TvdbEpisodeProvider: serieId = " + split[0] + ": Result: " + episode);

    byte[] poster = Downloader.tryReadURL("http://thetvdb.com/banners/episodes/" + split[0] + "/" + episode.getId() + ".jpg");

    Item item = new Item();

    item.setTitle(episode.getEpisodeName());
    item.setSeason(seasonNumber);
    item.setEpisode(episodeNumber);
    if(episode.getRating() != null) {
      item.setRating(Float.parseFloat(episode.getRating()));
    }
    item.setPlot(episode.getOverview());
    item.setPoster(poster);

    System.out.println(">>> Do something with this: first Aired = " + episode.getFirstAired());  // "2002-02-26"
    item.setLanguage(episode.getLanguage());

    return item;
  }
}
