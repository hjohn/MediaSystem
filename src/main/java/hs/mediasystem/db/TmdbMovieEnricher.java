package hs.mediasystem.db;

import hs.mediasystem.util.Levenshtein;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import net.sf.jtmdb.Movie;
import net.sf.jtmdb.MovieBackdrop;
import net.sf.jtmdb.MovieImages;
import net.sf.jtmdb.MoviePoster;

import org.json.JSONException;

public class TmdbMovieEnricher implements ItemEnricher {

  @Override
  public Identifier identifyItem(Item item) throws ItemNotFoundException {
    String title = item.getTitle();
    String subtitle = item.getSubtitle();
    String year = item.getReleaseYear() == null ? null : item.getReleaseYear().toString();
    int seq = item.getEpisode() == null ? 1 : item.getEpisode();

    try {
      String bestMatchingImdbNumber = null;

      if(item.getImdbId() != null) {
        bestMatchingImdbNumber = item.getImdbId();
      }

      if(bestMatchingImdbNumber == null) {
        TreeSet<Score> scores = new TreeSet<>(new Comparator<Score>() {
          @Override
          public int compare(Score o1, Score o2) {
            return Double.compare(o2.score, o1.score);
          }
        });

        String searchString = title;

        if(seq > 1) {
          searchString += " " + seq;
        }
        if(subtitle != null && subtitle.length() > 0) {
          searchString += " " + subtitle;
        }

        System.out.println("[FINE] TmdbMovieEnricher.identifyItem() - Looking to match: " + searchString);

        for(Movie movie : Movie.search(title)) {
          String movieYear = extractYear(movie.getReleasedDate());
          double score = 0;

          if(movieYear.equals(year) && movieYear.length() > 0) {
            score += 45;
          }
          if(movie.getImdbID() != null) {
            score += 15;
          }

          double matchScore = Levenshtein.compare(movie.getName().toLowerCase(), searchString.toLowerCase());

          score += matchScore * 40;

          scores.add(new Score(movie, score));
          String name = movie.getName() + (movie.getAlternativeName() != null ? " (" + movie.getAlternativeName() + ")" : "");
          System.out.println("[FINE] TmdbMovieEnricher.identifyItem() - " + String.format("Match: %5.1f (%4.2f) IMDB: %9s YEAR: %tY -- %s", score, matchScore, movie.getImdbID(), movie.getReleasedDate(), name));
        }

        if(!scores.isEmpty()) {
          bestMatchingImdbNumber = scores.first().movie.getImdbID();
          System.out.println("Best was: " + scores.first());
        }
      }

      if(bestMatchingImdbNumber != null) {
        return new Identifier("MOVIE", "TMDB", bestMatchingImdbNumber);
      }

      throw new ItemNotFoundException(item);
    }
    catch(IOException | JSONException e) {
      throw new ItemNotFoundException(item, e);
    }
  }

  @Override
  public Item enrichItem(Item item, Identifier identifier) throws ItemNotFoundException {
    if(identifier.getType().equals("MOVIE") && identifier.getProvider().equals("TMDB")) {
      String bestMatchingImdbNumber = identifier.getProviderId();

      try {
        System.out.println("best mathcing imdb number: " + bestMatchingImdbNumber);
        final Movie movie = Movie.imdbLookup(bestMatchingImdbNumber);

        System.out.println("Found movie:");
        System.out.println("name: " + movie.getName());  // TODO nullpointer here if IMDB is faulty (could be in filename)
        System.out.println("released date: " + movie.getReleasedDate());
        System.out.println("type: " + movie.getMovieType());
        System.out.println("overview: " + movie.getOverview());
        System.out.println("runtime: " + movie.getRuntime());

        final MovieImages images = movie.getImages();
        URL url = null;
        URL backgroundURL = null;

        if(images.posters.size() > 0) {
          MoviePoster poster = images.posters.iterator().next();

          url = poster.getLargestImage();
        }

        if(images.backdrops.size() > 0) {
          MovieBackdrop background = images.backdrops.iterator().next();

          backgroundURL = background.getLargestImage();
        }

        final byte[] poster = url != null ? Downloader.tryReadURL(url.toExternalForm()) : null;
        final byte[] background = backgroundURL != null ? Downloader.tryReadURL(backgroundURL.toExternalForm()) : null;

        item.setImdbId(movie.getImdbID());
        item.setTitle(movie.getName());
        item.setPoster(poster);
        item.setBackground(background);
        item.setPlot(movie.getOverview());
        item.setRating((float)movie.getRating());
        item.setReleaseDate(movie.getReleasedDate());
        item.setRuntime(movie.getRuntime());

        item.setType(identifier.getType());
        item.setProvider(identifier.getProvider());
        item.setProviderId(identifier.getProviderId());

  //          for(CastInfo castInfo : movie.getCast()) {
  //            castInfo.getCharacterName();
  //            castInfo.getID();
  //            castInfo.getName();
  //            castInfo.getThumb();
  //
  //            addCastMember(castInfo.getCastID(), castInfo.getName(), castInfo.getCharacterName());
  //          }

        return item;
      }
      catch(IOException | JSONException e) {
        throw new ItemNotFoundException(item, e);
      }
    }
    else {
      throw new RuntimeException("Unable to enrich, wrong provider or type: " + identifier);
    }
  }

  private static class Score {
    private final Movie movie;
    private final double score;

    public Score(Movie movie, double score) {
      this.movie = movie;
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
