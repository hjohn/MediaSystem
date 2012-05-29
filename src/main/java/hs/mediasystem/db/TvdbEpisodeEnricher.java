package hs.mediasystem.db;

import hs.mediasystem.db.MediaData.MatchType;
import hs.mediasystem.media.Media;
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
  public EnricherMatch identifyItem(final Media media) throws IdentifyException {
    hs.mediasystem.media.Episode episode = (hs.mediasystem.media.Episode)media;
    EnricherMatch serieMatch = itemIdentifier.identifyItem(episode.getSerie().getMedia());

    // TODO may need some TVDB caching here, as we're doing this query twice for each episode... and TVDB returns whole seasons I think
    EpisodeSearchResult result = findEpisode(serieMatch.getIdentifier().getProviderId(), episode);

    if(result == null) {
      throw new IdentifyException("unable to find episode with serieId " + serieMatch.getIdentifier().getProviderId() + " and " + episode);
    }

    return new EnricherMatch(new Identifier(media.getClass().getSimpleName(), getProviderCode(), serieMatch.getIdentifier().getProviderId() + "," + result.episode.getId()), result.matchType, result.matchAccuracy);  // TODO better would be episode id -- this is done here for specials, with season 0 and a nonsense episode number
  }

  @Override
  public Item loadItem(String identifier) throws ItemNotFoundException {
    String[] split = identifier.split(",");

    Episode episode = getEpisodeById(split[1]);

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

  private static EpisodeSearchResult findEpisode(String serieId, hs.mediasystem.media.Episode ep) {
    synchronized(TheTVDB.class) {
      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

      EpisodeSearchResult result;

      if(ep.getSeason() == null) {
        result = selectBestMatchByTitle(tvDB, serieId, ep.getTitle());
      }
      else {
        Episode episode = tvDB.getEpisode(serieId, ep.getSeason(), ep.getEpisode(), "en");

        result = new EpisodeSearchResult(episode, MatchType.ID, 1.0f);
      }

      System.out.println("[FINE] TvdbEpisodeProvider: serieId = " + serieId + ": Result: " + result);

      return result;
    }
  }

  private static Episode getEpisodeById(String id) {
    synchronized(TheTVDB.class) {
      TheTVDB tvDB = new TheTVDB("587C872C34FF8028");

      return tvDB.getEpisodeById(id, "en");
    }
  }

  private static EpisodeSearchResult selectBestMatchByTitle(TheTVDB tvDB, String key, String matchString) {
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

    return bestMatch == null ? null : new EpisodeSearchResult(bestMatch, MatchType.NAME, (float)bestScore);
  }

  private static class EpisodeSearchResult {
    private final Episode episode;
    private final MatchType matchType;
    private final float matchAccuracy;

    public EpisodeSearchResult(Episode episode, MatchType matchType, float matchAccuracy) {
      assert episode != null;
      assert matchType != null;
      assert matchAccuracy >= 0 && matchAccuracy <= 1.0;

      this.episode = episode;
      this.matchType = matchType;
      this.matchAccuracy = matchAccuracy;
    }
  }
}
