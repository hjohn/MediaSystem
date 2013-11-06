package hs.mediasystem.ext.media.movie;

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
import hs.mediasystem.util.CryptoUtil;
import hs.mediasystem.util.Levenshtein;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.moviejukebox.themoviedb.MovieDbException;
import com.moviejukebox.themoviedb.TheMovieDb;
import com.moviejukebox.themoviedb.model.Genre;
import com.moviejukebox.themoviedb.model.MovieDb;

public class TmdbMovieEnricher implements MediaIdentifier, MediaLoader {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private TheMovieDb TMDB;

  private TheMovieDb getTMDB() {
    try {
      if(TMDB == null) {
        TMDB = new TheMovieDb(CryptoUtil.decrypt("8AF22323DB8C0F235B38F578B7E09A61DB6F971EED59DE131E4EF70003CE84B483A778EBD28200A031F035F4209B61A4", "-MediaSystem-"));
      }

      return TMDB;
    }
    catch(MovieDbException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Identifier identifyItem(MediaItem mediaItem) throws IdentifyException {
    synchronized(TheMovieDb.class) {
      String title = mediaItem.title.get();
      String subtitle = mediaItem.subtitle.get() == null ? "" : mediaItem.subtitle.get();
      String imdb = (String)mediaItem.properties.get("imdbNumber");
      Integer year = (Integer)mediaItem.properties.get("releaseYear");
      int seq = Integer.parseInt(mediaItem.sequence.get() == null ? "1" : mediaItem.sequence.get());
      int tmdbMovieId = -1;

      try {
        float matchAccuracy = 1.0f;
        MatchType matchType = MatchType.ID;

        if(imdb == null) {
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

            System.out.println("[FINE] TmdbMovieEnricher.identifyItem() - Looking to match: " + searchString + "; year = " + year);

            for(MovieDb movieDb : getTMDB().searchMovie(searchString, "en", false)) {
              if(movieDb != null) {  // For "Robocop", this apparently can return null in the list??
                MatchType nameMatchType = MatchType.NAME;
                Integer movieYear = extractYear(parseDateOrNull(movieDb.getReleaseDate()));
                double score = 0;

                if(year != null && movieYear != null) {
                  if(year.equals(movieYear)) {
                    nameMatchType = MatchType.NAME_AND_YEAR;
                    score += 45;
                  }
                  else if(Math.abs(year - movieYear) == 1) {
                    score += 5;
                  }
                }

                double matchScore = Levenshtein.compare(movieDb.getTitle().toLowerCase(), searchString.toLowerCase());

                score += matchScore * 55;

                scores.add(new Score(movieDb, nameMatchType, score));
                String name = movieDb.getTitle() + (movieDb.getOriginalTitle() != null ? " (" + movieDb.getOriginalTitle() + ")" : "");
                System.out.println("[FINE] TmdbMovieEnricher.identifyItem() - " + String.format("Match: %5.1f (%4.2f) IMDB: %9s YEAR: %s -- %s", score, matchScore, movieDb.getImdbID(), movieDb.getReleaseDate(), name));
              }
            }

            if(!scores.isEmpty()) {
              Score bestScore = scores.first();

              tmdbMovieId = bestScore.movie.getId();
              matchType = bestScore.matchType;
              matchAccuracy = (float)(bestScore.score / 100);
            }
          }
        }
        else {
          final MovieDb movieDb = getTMDB().getMovieInfoImdb(imdb, "en");

          if(movieDb != null) {
            tmdbMovieId = movieDb.getId();
          }
        }

        if(tmdbMovieId != -1) {
          return new Identifier(new ProviderId(mediaItem.getDataType().getSimpleName(), "TMDB", Integer.toString(tmdbMovieId)), matchType, matchAccuracy);
        }

        throw new IdentifyException(mediaItem);
      }
      catch(MovieDbException e) {
        throw new IdentifyException(mediaItem, e);
      }
    }
  }

  @Override
  public Item loadItem(ProviderId providerId) throws ItemNotFoundException {
    synchronized(Movie.class) {
      try {
        System.out.println("[FINE] TmdbMovieEnricher.loadItem() - tmdb.id = " + providerId);

        int tmdbId = Integer.parseInt(providerId.getId());

        MovieDb movie = getTMDB().getMovieInfo(tmdbId, "en");

        if(movie == null) {
          throw new ItemNotFoundException("TMDB lookup by tmdb.id failed: " + providerId);
        }

        System.out.println("[FINE] TmdbMovieEnricher.loadItem() - Found: name=" + movie.getTitle() + "; release date=" + movie.getReleaseDate() + "; runtime=" + movie.getRuntime() + "; popularity=" + movie.getPopularity() + "; language=" + movie.getSpokenLanguages() + "; tagline=" + movie.getTagline() + "; genres=" + movie.getGenres());

        URL posterURL = getTMDB().createImageUrl(movie.getPosterPath(), "original");
        URL backgroundURL = getTMDB().createImageUrl(movie.getBackdropPath(), "original");

        Item item = new Item(providerId);

        item.setImdbId(movie.getImdbID());
        item.setTitle(movie.getTitle());
        item.setPlot(movie.getOverview());
        item.setRating(movie.getVoteAverage());
        item.setReleaseDate(parseDateOrNull(movie.getReleaseDate()));
        item.setRuntime(movie.getRuntime());
        item.setTagline(movie.getTagline());
        if(!movie.getSpokenLanguages().isEmpty()) {
          item.setLanguage(movie.getSpokenLanguages().get(0).getName());
        }

        item.setBackgroundURL(backgroundURL == null ? null : backgroundURL.toExternalForm());
        item.setBannerURL(null);
        item.setPosterURL(posterURL == null ? null : posterURL.toExternalForm());

        List<String> genres = new ArrayList<>();

        for(Genre genre : movie.getGenres()) {
          genres.add(genre.getName());
        }

        item.setGenres(genres.toArray(new String[genres.size()]));

        TreeSet<com.moviejukebox.themoviedb.model.Person> movieCasts = new TreeSet<>(new Comparator<com.moviejukebox.themoviedb.model.Person>() {
          @Override
          public int compare(com.moviejukebox.themoviedb.model.Person o1, com.moviejukebox.themoviedb.model.Person o2) {
            int result = o1.getName().compareTo(o2.getName());

            if(result == 0) {
              result = o1.getJob().compareTo(o2.getJob());
            }

            return result;
          }
        });

        movieCasts.addAll(getTMDB().getMovieCasts(tmdbId));  // eliminates duplicates

        for(com.moviejukebox.themoviedb.model.Person tmdbPerson : movieCasts) {
          Person person = new Person();

          person.setName(tmdbPerson.getName());
          if(tmdbPerson.getProfilePath() != null) {
            URL largestImageUrl = getTMDB().createImageUrl(tmdbPerson.getProfilePath(), "original");

            if(largestImageUrl != null) {
              person.setPhotoURL(largestImageUrl.toExternalForm());
            }
          }
          //person.setBiography(tmdbPerson.getBiography());
          //person.setBirthPlace(tmdbPerson.getBirthplace());
          //person.setBirthDate(personInfo.getBirthday());

          Casting casting = new Casting();

          casting.setItem(item);
          casting.setPerson(person);
          casting.setRole(tmdbPerson.getJob().equals("actor") ? "Actor" : tmdbPerson.getJob());
          casting.setCharacterName(tmdbPerson.getCharacter());
          casting.setIndex(tmdbPerson.getOrder());

          item.getCastings().add(casting);
        }

        return item;
      }
      catch(MovieDbException e) {
        throw new ItemNotFoundException(providerId, e);
      }
    }
  }

  private static class Score {
    private final MovieDb movie;
    private final MatchType matchType;
    private final double score;

    public Score(MovieDb movie, MatchType matchType, double score) {
      this.movie = movie;
      this.matchType = matchType;
      this.score = score;
    }

    @Override
    public String toString() {
      return String.format("Score[%10.2f, " + movie.getImdbID() + " : " + movie.getTitle() + " : " + movie.getReleaseDate() + "]", score);
    }
  }

  private static LocalDate parseDateOrNull(String text) {
    try {
      return DATE_TIME_FORMATTER.parse(text, new TemporalQuery<LocalDate>() {
        @Override
        public LocalDate queryFrom(TemporalAccessor temporal) {
          return LocalDate.from(temporal);
        }
      });
    }
    catch(DateTimeParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Integer extractYear(LocalDate date) {
    if(date == null) {
      return null;
    }

    return date.getYear();
  }
}
