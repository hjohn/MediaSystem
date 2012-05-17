package hs.mediasystem.db;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.Levenshtein;

import java.util.List;

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
  public String identifyItem(final MediaItem mediaItem) throws IdentifyException {
    String serieId = itemIdentifier.identifyItem(mediaItem.getGroupName());

    // TODO may need some TVDB caching here, as we're doing this query twice for each episode... and TVDB returns whole seasons I think
    Episode episode = findEpisode(serieId, mediaItem);

    return serieId + "," + episode.getSeasonNumber() + "," + episode.getEpisodeNumber();  // TODO better would be episode id -- this is done here for specials, with season 0 and a nonsense episode number
  }

  @Override
  public Item loadItem(String identifier, MediaItem mediaItem) throws ItemNotFoundException {
    String[] split = identifier.split(",");

    Episode episode = findEpisode(split[0], mediaItem);

    Item item = new Item();

    item.setTitle(episode.getEpisodeName());
    item.setSeason(episode.getSeasonNumber());
    item.setEpisode(episode.getEpisodeNumber());
    if(episode.getRating() != null && !episode.getRating().isEmpty()) {
      item.setRating(Float.parseFloat(episode.getRating()));
    }
    item.setPlot(episode.getOverview());

    item.setBackgroundURL(null);
    item.setBannerURL(null);
    item.setPosterURL("http://thetvdb.com/banners/episodes/" + split[0] + "/" + episode.getId() + ".jpg");

    System.out.println(">>> Do something with this: first Aired = " + episode.getFirstAired());  // "2002-02-26"
    item.setLanguage(episode.getLanguage());

    for(String director : episode.getDirectors()) {
      Person person = new Person();

      person.setName(director);

      Casting casting = new Casting();

      casting.setItem(item);
      casting.setPerson(person);
      casting.setRole("director");

      item.getCastings().add(casting);
    }

    for(String guestStar : episode.getGuestStars()) {
      Person person = new Person();

      person.setName(guestStar);

      Casting casting = new Casting();

      casting.setItem(item);
      casting.setPerson(person);
      casting.setRole("actor");

      item.getCastings().add(casting);
    }

    return item;
  }

  private static Episode findEpisode(String serieId, MediaItem mediaItem) {
    synchronized(TheTVDB.class) {
      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

      Episode episode;

      if(mediaItem.getSeason() == null) {
        episode = selectBestMatchByTitle(tvDB, serieId, mediaItem.getTitle());
      }
      else {
        episode = tvDB.getEpisode(serieId, mediaItem.getSeason(), mediaItem.getEpisode(), "en");
      }

      System.out.println("[FINE] TvdbEpisodeProvider: serieId = " + serieId + ": Result: " + episode);

      return episode;
    }
  }

  private static Episode selectBestMatchByTitle(TheTVDB tvDB, String key, String matchString) {
    List<Episode> episodes = tvDB.getSeasonEpisodes(key, 0, "en");
    Episode bestMatch = null;
    double bestScore = -1;

    System.out.println("[FINE] TvdbEpisodeEnricher.selectBestMatch() - Looking to match: " + matchString);

    for(Episode episode : episodes) {
      double matchScore = Levenshtein.compare(episode.getEpisodeName(), matchString);

      if(matchScore > bestScore) {
        bestMatch = episode;
        bestScore = matchScore;
      }

      System.out.println("[FINE] TvdbEpisodeEnricher.selectBestMatch() - " + String.format("Match: %5.1f (%4.2f) -- %s", matchScore * 100, matchScore, episode.getEpisodeName()));
    }

    return bestMatch;
  }
}
