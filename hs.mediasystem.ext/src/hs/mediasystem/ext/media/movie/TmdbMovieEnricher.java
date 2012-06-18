package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.IdentifyException;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemEnricher;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.framework.Media;
import hs.mediasystem.util.Levenshtein;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import net.sf.jtmdb.CastInfo;
import net.sf.jtmdb.Genre;
import net.sf.jtmdb.Movie;
import net.sf.jtmdb.MovieBackdrop;
import net.sf.jtmdb.MovieImages;
import net.sf.jtmdb.MoviePoster;

import org.json.JSONException;

public class TmdbMovieEnricher implements ItemEnricher {

  @Override
  public String getProviderCode() {
    return "TMDB";
  }

  @Override
  public Identifier identifyItem(Media media) throws IdentifyException {
    synchronized(Movie.class) {
      MovieBase movieBase = (MovieBase)media;

      String title = movieBase.getGroupTitle();
      String subtitle = movieBase.getSubtitle();
      String year = movieBase.getReleaseYear() == null ? null : movieBase.getReleaseYear().toString();
      int seq = movieBase.getSequence() == null ? 1 : movieBase.getSequence();

      try {
        String bestMatchingImdbNumber = movieBase.getImdbNumber();

        float matchAccuracy = 1.0f;
        MatchType matchType = MatchType.ID;

//        if(bestMatchingImdbNumber == null) {
//          System.out.println(">>> Trying to match on hash: " + Long.toHexString(mediaItem.getMediaId().getOsHash()));
//          List<Movie> movies = Media.getInfo(Long.toHexString(mediaItem.getMediaId().getOsHash()), mediaItem.getMediaId().getFileLength());
//
//          if(movies != null && !movies.isEmpty()) {
//            System.out.println("[FINE] TmdbMovieEnricher.identifyItem() - Found match by Hash for " + mediaItem + ": " + movies.get(0));
//            System.out.println(">>> Wow MATCH!");
//
//            matchType = MatchType.HASH;
//            bestMatchingImdbNumber = movies.get(0).getImdbID();
//          }
//        }

        if(bestMatchingImdbNumber == null) {
          TreeSet<Score> scores = new TreeSet<>(new Comparator<Score>() {
            @Override
            public int compare(Score o1, Score o2) {
              return Double.compare(o2.score, o1.score);
            }
          });

          List<String> variations = new ArrayList<>();

          variations.add(title);
          if(title.contains(", ")) {
            int comma = title.indexOf(", ");

            variations.add(title.substring(comma + 2) + " " + title.substring(0, comma));
          }

          for(String variation : variations) {
            String searchString = variation;

            if(seq > 1) {
              searchString += " " + seq;
            }
            if(subtitle.length() > 0) {
              searchString += " " + subtitle;
            }

            System.out.println("[FINE] TmdbMovieEnricher.identifyItem() - Looking to match: " + searchString);

            for(Movie movie : Movie.search(searchString)) {
              MatchType nameMatchType = MatchType.NAME;
              String movieYear = extractYear(movie.getReleasedDate());
              double score = 0;

              if(movieYear.equals(year) && movieYear.length() > 0) {
                nameMatchType = MatchType.NAME_AND_YEAR;
                score += 45;
              }
              if(movie.getImdbID() != null) {
                score += 15;
              }

              double matchScore = Levenshtein.compare(movie.getName().toLowerCase(), searchString.toLowerCase());

              score += matchScore * 40;

              scores.add(new Score(movie, nameMatchType, score));
              String name = movie.getName() + (movie.getAlternativeName() != null ? " (" + movie.getAlternativeName() + ")" : "");
              System.out.println("[FINE] TmdbMovieEnricher.identifyItem() - " + String.format("Match: %5.1f (%4.2f) IMDB: %9s YEAR: %tY -- %s", score, matchScore, movie.getImdbID(), movie.getReleasedDate(), name));
            }

            if(!scores.isEmpty()) {
              Score bestScore = scores.first();

              bestMatchingImdbNumber = bestScore.movie.getImdbID();
              matchType = bestScore.matchType;
              matchAccuracy = (float)(bestScore.score / 100);
            }
          }
        }

        if(bestMatchingImdbNumber != null) {
          return new Identifier(media.getClass().getSimpleName(), getProviderCode(), bestMatchingImdbNumber, matchType, matchAccuracy);
        }

        throw new IdentifyException(media);
      }
      catch(IOException | JSONException e) {
        throw new IdentifyException(media, e);
      }
    }
  }

  @Override
  public Item loadItem(String identifier) throws ItemNotFoundException {
    synchronized(Movie.class) {
      String bestMatchingImdbNumber = identifier;

      try {
        System.out.println("[FINE] TmdbMovieEnricher.loadItem() - imdb = " + bestMatchingImdbNumber);

        final Movie movie = Movie.imdbLookup(bestMatchingImdbNumber);

        if(movie == null) {
          throw new ItemNotFoundException("TMDB lookup by IMDB id failed: " + identifier);
        }

        System.out.println("[FINE] TmdbMovieEnricher.loadItem() - Found: name=" + movie.getName() + "; release date=" + movie.getReleasedDate() + "; runtime=" + movie.getRuntime() + "; type=" + movie.getMovieType() + "; language=" + movie.getLanguage() + "; tagline=" + movie.getTagline() + "; genres=" + movie.getGenres());

        final MovieImages images = movie.getImages();
        URL posterURL = null;
        URL backgroundURL = null;

        if(images.posters.size() > 0) {
          MoviePoster poster = images.posters.iterator().next();

          posterURL = poster.getLargestImage();
        }

        if(images.backdrops.size() > 0) {
          MovieBackdrop background = images.backdrops.iterator().next();

          backgroundURL = background.getLargestImage();
        }

        Item item = new Item();

        item.setImdbId(movie.getImdbID());
        item.setTitle(movie.getName());
        item.setPlot(movie.getOverview());
        item.setRating((float)movie.getRating());
        item.setReleaseDate(movie.getReleasedDate());
        item.setRuntime(movie.getRuntime());
        item.setTagline(movie.getTagline());
        item.setLanguage(movie.getLanguage());

        item.setBackgroundURL(backgroundURL == null ? null : backgroundURL.toExternalForm());
        item.setBannerURL(null);
        item.setPosterURL(posterURL == null ? null : posterURL.toExternalForm());

        List<String> genres = new ArrayList<>();

        for(Genre genre : movie.getGenres()) {
          genres.add(genre.getName());
        }

        item.setGenres(genres.toArray(new String[genres.size()]));

        System.out.println(">>> TmdbMovieEnricher: type = " + movie.getMovieType() + "; certification = " + movie.getCertification() + "; votes = " + movie.getVotes() + "; last mod date = " + movie.getLastModifiedAtDate());

        for(CastInfo castInfo : movie.getCast()) {
          System.out.println(">>> TmdbMovieEnricher: Cast: id/castid = " + castInfo.getID() + "/" + castInfo.getCastID() + " : " + castInfo.getCharacterName() + " => " + castInfo.getName() + " ; " + castInfo.getJob() + " ; " + castInfo.getJob());
        }

        return item;
      }
      catch(IOException | JSONException e) {
        throw new ItemNotFoundException(identifier, e);
      }
    }
  }

  private static class Score {
    private final Movie movie;
    private final MatchType matchType;
    private final double score;

    public Score(Movie movie, MatchType matchType, double score) {
      this.movie = movie;
      this.matchType = matchType;
      this.score = score;
    }

    @Override
    public String toString() {
      return String.format("Score[%10.2f, " + movie.getImdbID() + " : " + movie.getName() + " : " + movie.getReleasedDate() + "]", score);
    }
  }

  private static String extractYear(Date date) {
    if(date == null) {
      return "";
    }

    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(date);
    return "" + gc.get(Calendar.YEAR);
  }
}
