package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.ext.media.movie.Movie;
import hs.mediasystem.framework.Identifier;
import hs.mediasystem.framework.MediaIdentifier;
import hs.mediasystem.util.Levenshtein;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.jackson.JsonNode;

@Named
public class TmdbMovieMediaIdentifier extends MediaIdentifier<Movie> {
  private final TheMovieDatabase tmdb;

  @Inject
  public TmdbMovieMediaIdentifier(TheMovieDatabase tmdb) {
    super("TMDB", "Movie");

    this.tmdb = tmdb;
  }

  @Override
  public Identifier identify(Movie movie) {
    SearchResult searchResult = searchForMovie(movie);

    if(searchResult != null) {
      return new Identifier().setAll(
        new ProviderId("Movie", "TMDB", Integer.toString(searchResult.tmdbId)),
        searchResult.matchType,
        searchResult.matchAccuracy
      );
    }

    return null;
  }

  private SearchResult searchForMovie(Movie movie) {
    synchronized(TheMovieDatabase.class) {
      String title = movie.localTitle.get();
      String subtitle = movie.subtitle.get() == null ? "" : movie.subtitle.get();
      String imdb = movie.imdbNumber.get();
      Integer year = movie.localReleaseYear.get() == null ? null : Integer.parseInt(movie.localReleaseYear.get());
      int seq = movie.sequence.get() == null ? 1 : movie.sequence.get();
      int tmdbMovieId = -1;
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

          System.out.println("[FINE] " + getClass().getName() + ": Looking to match: " + searchString + "; year = " + year);
          JsonNode node = tmdb.query("3/search/movie", "query", searchString, "language", "en");

          for(Iterator<JsonNode> nodeIterator = node.path("results").iterator(); nodeIterator.hasNext(); ) {
            JsonNode resultNode = nodeIterator.next();

            String nodeTitle = resultNode.path("title").asText();
            String nodeOriginalTitle = resultNode.path("original_title").asText();

            MatchType nameMatchType = MatchType.NAME;
            LocalDate releaseDate = TheMovieDatabase.parseDateOrNull(resultNode.path("release_date").asText());
            Integer movieYear = extractYear(releaseDate);
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

            double matchScore = Levenshtein.compare(nodeTitle.toLowerCase(), searchString.toLowerCase());

            score += matchScore * 55;

            scores.add(new Score(resultNode, nameMatchType, score));
            String name = nodeTitle + (nodeOriginalTitle != null ? " (" + nodeOriginalTitle + ")" : "");
            System.out.println("[FINE] " + getClass().getName() + ": " + String.format("Match: %5.1f (%4.2f) YEAR: %s -- %s", score, matchScore, "" + releaseDate, name));
          }

          if(!scores.isEmpty()) {
            Score bestScore = scores.first();

            tmdbMovieId = bestScore.movie.get("id").asInt();
            matchType = bestScore.matchType;
            matchAccuracy = (float)(bestScore.score / 100);
          }
        }
      }
      else {
        JsonNode node = tmdb.query("3/find/" + imdb, "external_source", "imdb_id");

        for(Iterator<JsonNode> nodeIterator = node.path("movie_results").iterator(); nodeIterator.hasNext(); ) {
          JsonNode resultNode = nodeIterator.next();

          tmdbMovieId = resultNode.get("id").asInt();
          break;
        }
      }

      if(tmdbMovieId != -1) {
        return new SearchResult(tmdbMovieId, matchType, matchAccuracy);
      }

      return null;
    }
  }

  private static Integer extractYear(LocalDate date) {
    if(date == null) {
      return null;
    }

    return date.getYear();
  }

  private static class Score {
    private final JsonNode movie;
    private final MatchType matchType;
    private final double score;

    public Score(JsonNode movie, MatchType matchType, double score) {
      this.movie = movie;
      this.matchType = matchType;
      this.score = score;
    }

    @Override
    public String toString() {
      return String.format("Score[%10.2f, " + movie.path("title").asText() + " : " + movie.path("release_date").asText() + "]", score);
    }
  }

  private static class SearchResult {
    private final int tmdbId;
    private final MatchType matchType;
    private final float matchAccuracy;

    public SearchResult(int tmdbId, MatchType matchType, double matchAccuracy) {
      this.tmdbId = tmdbId;
      this.matchType = matchType;
      this.matchAccuracy = (float)matchAccuracy;
    }
  }
}
