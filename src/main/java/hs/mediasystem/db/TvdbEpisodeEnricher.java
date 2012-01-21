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
  public Identifier identifyItem(final Item item) throws ItemNotFoundException {
    Item serieItem = new Item() {{
      setTitle(item.getTitle());
      setType("SERIE");
    }};

    Identifier identifier = itemIdentifier.identifyItem(serieItem);

    return new Identifier("EPISODE", "TVDB", identifier.getProviderId() + "," + item.getSeason() + "," + item.getEpisode());
  }

  @Override
  public Item enrichItem(final Item item, Identifier identifier) throws ItemNotFoundException {
    if(identifier.getType().equals("EPISODE") && identifier.getProvider().equals("TVDB")) {
      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

      String[] split = identifier.getProviderId().split(",");

      final Episode episode = tvDB.getEpisode(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), "en");

      System.out.println("[FINE] TvdbEpisodeProvider: serieId = " + split[0] + ": Result: " + episode);

      byte[] poster = Downloader.tryReadURL("http://thetvdb.com/banners/episodes/" + split[0] + "/" + episode.getId() + ".jpg");

      item.setPlot(episode.getOverview());
      if(episode.getRating() != null) {
        item.setRating(Float.parseFloat(episode.getRating()));
      }
      item.setTitle(item.getTitle());
      item.setSubtitle(episode.getEpisodeName());
      item.setSeason(item.getSeason());
      item.setEpisode(item.getEpisode());
      item.setPoster(poster);

      item.setType(identifier.getType());
      item.setProvider(identifier.getProvider());
      item.setProviderId(identifier.getProviderId());

      return item;
    }
    else {
      throw new RuntimeException("Unable to enrich, wrong provider or type: " + identifier);
    }
  }
}
