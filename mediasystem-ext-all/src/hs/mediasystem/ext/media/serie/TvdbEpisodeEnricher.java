package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Casting;
import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.Person;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.framework.IdentifyException;
import hs.mediasystem.framework.MediaIdentifier;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaLoader;
import hs.mediasystem.util.Levenshtein;

import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Episode;

public class TvdbEpisodeEnricher implements MediaIdentifier, MediaLoader {
  private final TvdbSerieEnricher itemIdentifier;

  @Inject
  public TvdbEpisodeEnricher(TvdbSerieEnricher itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
  }

  @Override
  public Identifier identifyItem(final MediaItem mediaItem) throws IdentifyException {
    MediaItem serie = (MediaItem)mediaItem.properties.get("serie");
    Identifier serieMatch = itemIdentifier.identifyItem(serie);

    // TODO may need some TVDB caching here, as we're doing this query twice for each episode... and TVDB returns whole seasons I think
    EpisodeSearchResult result = findEpisode(serieMatch.getProviderId().getId(), mediaItem);

    if(result == null) {
      throw new IdentifyException("unable to find episode with serieId " + serieMatch.getProviderId() + " and " + mediaItem);
    }

    return new Identifier(new ProviderId(mediaItem.getDataType().getSimpleName(), "TVDB", serieMatch.getProviderId().getId() + "," + result.episode.getId()), result.matchType, result.matchAccuracy);  // TODO better would be episode id -- this is done here for specials, with season 0 and a nonsense episode number
  }

  @Override
  public Item loadItem(ProviderId providerId) throws ItemNotFoundException {
    String[] split = providerId.getId().split(",");

    Episode episode = getEpisodeById(split[1]);

    Item item = new Item(providerId);

    item.setTitle(episode.getEpisodeName());
    item.setSeason(episode.getSeasonNumber());
    item.setEpisode(episode.getEpisodeNumber());
    if(episode.getRating() != null && !episode.getRating().isEmpty()) {
      item.setRating(Float.valueOf(episode.getRating()));
    }
    item.setPlot(episode.getOverview());

    item.setBackgroundURL(null);
    item.setBannerURL(null);
    item.setPosterURL("http://thetvdb.com/banners/episodes/" + split[0] + "/" + episode.getId() + ".jpg");

    System.out.println(">>> Do something with this: first Aired = " + episode.getFirstAired());  // "2002-02-26"
    item.setLanguage(episode.getLanguage());

    for(String director : new HashSet<>(episode.getDirectors())) {  // Wrap with HashSet removes duplicate names
      Person person = new Person();

      person.setName(director);

      Casting casting = new Casting();

      casting.setItem(item);
      casting.setPerson(person);
      casting.setRole("Director");

      item.getCastings().add(casting);
    }

    for(String guestStar : new HashSet<>(episode.getGuestStars())) {  // Wrap with HashSet removes duplicate names
      Person person = new Person();

      person.setName(guestStar);

      Casting casting = new Casting();

      casting.setItem(item);
      casting.setPerson(person);
      casting.setRole("Actor");

      item.getCastings().add(casting);
    }

    return item;
  }

  private static EpisodeSearchResult findEpisode(String serieId, MediaItem ep) {
    synchronized(TheTVDB.class) {
      EpisodeSearchResult result = null;
      Integer season = (Integer)ep.properties.get("season");
      Integer episodeNumber = (Integer)ep.properties.get("episodeNumber");

      if(season == null) {
        result = selectBestMatchByTitle(TvdbSerieEnricher.TVDB, serieId, ep.title.get());
      }
      else {
        Episode episode = TvdbSerieEnricher.TVDB.getEpisode(serieId, season, episodeNumber, "en");

        if(episode != null) {
          result = new EpisodeSearchResult(episode, MatchType.ID, 1.0f);
        }
      }

      System.out.println("[FINE] TvdbEpisodeProvider: serieId = " + serieId + ": Result: " + result);

      return result;
    }
  }

  private static Episode getEpisodeById(String id) {
    synchronized(TheTVDB.class) {
      return TvdbSerieEnricher.TVDB.getEpisodeById(id, "en");
    }
  }

  private static EpisodeSearchResult selectBestMatchByTitle(TheTVDB tvDB, String key, String matchString) {
    List<Episode> episodes = tvDB.getSeasonEpisodes(key, 0, "en");
    Episode bestMatch = null;
    double bestScore = -1;

    System.out.println("[FINE] TvdbEpisodeEnricher.selectBestMatch() - Looking to match: " + matchString);

    if(episodes != null) {
      for(Episode episode : episodes) {
        double matchScore = Levenshtein.compare(episode.getEpisodeName(), matchString);

        if(matchScore > bestScore) {
          bestMatch = episode;
          bestScore = matchScore;
        }

        System.out.println("[FINE] TvdbEpisodeEnricher.selectBestMatch() - " + String.format("Match: %5.1f (%4.2f) -- %s", matchScore * 100, matchScore, episode.getEpisodeName()));
      }
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
