package hs.mediasystem.ext.serie;

import hs.mediasystem.db.Casting;
import hs.mediasystem.db.EnricherMatch;
import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.MediaData.MatchType;
import hs.mediasystem.db.Person;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.SerieBase;
import hs.mediasystem.util.CryptoUtil;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.moviejukebox.thetvdb.TheTVDB;
import com.moviejukebox.thetvdb.model.Actor;
import com.moviejukebox.thetvdb.model.Series;

public class TvdbSerieEnricher implements ItemEnricher {
  static final TheTVDB TVDB = new TheTVDB(CryptoUtil.decrypt("E6A6CF878B4A6200A66E31AED48627CE83A778EBD28200A031F035F4209B61A4", "-MediaSystem-"));

  @Override
  public String getProviderCode() {
    return "TVDB";
  }

  @Override
  public EnricherMatch identifyItem(final Media media) throws IdentifyException {
    synchronized(TheTVDB.class) {
      List<Series> results = TVDB.searchSeries(media.getTitle(), "en");

      System.out.println("TVDB results: " + results);

      if(results.isEmpty()) {
        throw new IdentifyException("Cannot identify Serie with name: " + media.getTitle());
      }

      return new EnricherMatch(new Identifier(SerieBase.class.getSimpleName(), getProviderCode(), results.get(0).getId()), MatchType.NAME, 1.0f);
    }
  }

  @Override
  public Item loadItem(String identifier) throws ItemNotFoundException {
    synchronized(TheTVDB.class) {
      final Series series = TVDB.getSeries(identifier, "en");

      if(series == null) {
        throw new ItemNotFoundException(identifier);
      }

      Item item = new Item();

      item.setTitle(series.getSeriesName());
      item.setRating(Float.valueOf(series.getRating()));
      item.setPlot(series.getOverview());

      item.setBackgroundURL(series.getFanart());
      item.setBannerURL(series.getBanner());
      item.setPosterURL(series.getPoster());

      item.setGenres(series.getGenres().toArray(new String[series.getGenres().size()]));
      item.setLanguage(series.getLanguage());
      item.setRuntime(Integer.parseInt(series.getRuntime()));

      TreeSet<Actor> actors = new TreeSet<>(new Comparator<Actor>() {
        @Override
        public int compare(Actor o1, Actor o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });

      actors.addAll(TVDB.getActors(identifier));  // de-duplicates the actors we get from tvdb

      for(Actor actor : actors) {
        Person person = new Person();

        person.setName(actor.getName());
        person.setPhotoURL(actor.getImage());

        Casting casting = new Casting();

        casting.setItem(item);
        casting.setPerson(person);
        casting.setRole("actor");
        casting.setCharacterName(actor.getRole());
        casting.setIndex(actor.getSortOrder());

        item.getCastings().add(casting);
      }

      System.out.println(">>> Status of serie : " + series.getStatus());  // "Ended", "Continuing"
      System.out.println(">>> First aired : " +  series.getFirstAired());

      return item;
    }
  }
}
