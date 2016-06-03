package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.ext.media.movie.Movie;
import hs.mediasystem.framework.Identifier;
import hs.mediasystem.framework.MediaIdentifier;
import hs.mediasystem.util.Levenshtein;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.jackson.JsonNode;

@Named
public class TmdbMovieMediaIdentifier extends MediaIdentifier<Movie> {
  private static final Pattern TITLES = Pattern.compile("(.*?)(?: \\((.*?)\\))?");

  private final TheMovieDatabase tmdb;

  @Inject
  public TmdbMovieMediaIdentifier(TheMovieDatabase tmdb) {
    super("TMDB", "Movie");

    this.tmdb = tmdb;
  }

  @Override
  public Identifier identify(Movie movie) {
    Match match = searchForMovie(movie);

    if(match != null) {
      return new Identifier().setAll(
        new ProviderId("Movie", "TMDB", match.movie.get("id").asText()),
        match.type,
        (float)match.score / 100
      );
    }

    return null;
  }

  private Match searchForMovie(Movie movie) {
    synchronized(TheMovieDatabase.class) {
      String imdb = movie.initialImdbNumber.get();

      if(imdb == null) {
        List<String> titleVariations = createVariations(movie.initialTitle.get());

        String subtitle = movie.initialSubtitle.get() == null ? "" : movie.initialSubtitle.get();
        Integer year = movie.localReleaseYear.get() == null ? null : Integer.parseInt(movie.localReleaseYear.get());
        int seq = movie.sequence.get() == null ? 1 : movie.sequence.get();

        String postFix = (seq > 1 ? " " + seq : "") + (!subtitle.isEmpty() ? " " + subtitle : "");

        return titleVariations.stream()
          .map(tv -> tv + postFix)
          .peek(q -> System.out.println("[FINE] " + getClass().getName() + ": Looking to match: '" + q + "'; year = " + year))
          .flatMap(q -> StreamSupport.stream(tmdb.query("3/search/movie", "query", q, "language", "en").path("results").spliterator(), false)
            .flatMap(jsonNode -> Stream.of(jsonNode.path("title").asText(), jsonNode.path("original_title").asText())
              .filter(t -> !t.isEmpty())
              .distinct()
              .map(t -> createMatch(jsonNode, q.toLowerCase(), year, t))
              .peek(m -> System.out.println("[FINE] " + getClass().getName() + ": " + m))
            )
          )
          .max(Comparator.comparingDouble(Match::getScore))
          .orElse(null);
      }

      JsonNode node = tmdb.query("3/find/" + imdb, "external_source", "imdb_id");

      return StreamSupport.stream(node.path("movie_results").spliterator(), false)
        .findFirst()
        .map(n -> new Match(n, MatchType.ID, 100))
        .orElse(null);
    }
  }

  Match createMatch(JsonNode resultNode, String titleToMatch, Integer year, String nodeTitle) {
    LocalDate releaseDate = TheMovieDatabase.parseDateOrNull(resultNode.path("release_date").asText());
    Integer movieYear = extractYear(releaseDate);

    MatchType nameMatchType = MatchType.NAME;
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

    double matchScore = Levenshtein.compare(nodeTitle.toLowerCase(), titleToMatch);

    score += matchScore * 55;

    return new Match(resultNode, nameMatchType, score);
  }

  List<String> createVariations(String fullTitle) {
    Matcher matcher = TITLES.matcher(fullTitle);

    if(!matcher.matches()) {
      throw new IllegalStateException("title did not match pattern: " + fullTitle);
    }

    String title = matcher.group(1);
    String secondaryTitle = matcher.group(2);  // Translated title, or alternative title

    List<String> variations = new ArrayList<>();

    variations.add(title);
    variations.addAll(createPronounVariations(title));

    if(secondaryTitle != null) {
      variations.add(secondaryTitle);
      variations.addAll(createPronounVariations(secondaryTitle));
    }

    return variations;
  }

  private static List<String> createPronounVariations(String title) {
    int comma = title.indexOf(", ");

    if(comma < 0) {
      return Collections.emptyList();
    }

    List<String> variations = new ArrayList<>();

    variations.add(title.substring(comma + 2) + " " + title.substring(0, comma));  // With pronoun at start
    variations.add(title.substring(0, comma));  // Without pronoun

    return variations;
  }

  private static Integer extractYear(LocalDate date) {
    if(date == null) {
      return null;
    }

    return date.getYear();
  }

  private static class Match {
    private final JsonNode movie;
    private final MatchType type;
    private final double score;

    public Match(JsonNode movie, MatchType matchType, double score) {
      this.movie = movie;
      this.type = matchType;
      this.score = score;
    }

    public double getScore() {
      return score;
    }

    @Override
    public String toString() {
      String nodeOriginalTitle = movie.path("original_title").asText();
      String name = movie.path("title").asText() + (nodeOriginalTitle != null ? " (" + nodeOriginalTitle + ")" : "");

      return String.format("Match[%6.2f, " + name + " : " + movie.path("release_date").asText() + "]", score);
    }
  }
}
