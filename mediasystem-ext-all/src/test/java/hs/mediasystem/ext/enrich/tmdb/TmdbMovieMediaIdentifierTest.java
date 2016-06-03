package hs.mediasystem.ext.enrich.tmdb;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.ext.media.movie.Movie;
import hs.mediasystem.framework.Identifier;

import java.io.IOException;
import java.util.Arrays;

import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TmdbMovieMediaIdentifierTest {
  private Movie movie = new Movie();
  private ObjectMapper objectMapper = new ObjectMapper();

  @Mock private TheMovieDatabase tmdb;
  @InjectMocks private TmdbMovieMediaIdentifier identifier;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldIdentifyMovies() throws JsonProcessingException, IOException {
    when(tmdb.query(eq("3/search/movie"), eq("query"), eq("Terminator 5 Genisys"), eq("language"), eq("en"))).thenReturn(objectMapper.readTree("{\"results\":[{\"id\":80000,\"original_title\":\"Terminator Genisys\",\"release_date\":\"2015-07-01\",\"title\":\"Terminator Genisys\"}]}"));
    when(tmdb.query(eq("3/search/movie"), eq("query"), eq("Terminator, The 5 Genisys"), eq("language"), eq("en"))).thenReturn(objectMapper.readTree("{}"));
    when(tmdb.query(eq("3/search/movie"), eq("query"), eq("The Terminator 5 Genisys"), eq("language"), eq("en"))).thenReturn(objectMapper.readTree("{}"));

    movie.initialTitle.set("Terminator, The");
    movie.localReleaseYear.set("2015");
    movie.sequence.set(5);
    movie.initialSubtitle.set("Genisys");

    Identifier i = identifier.identify(movie);

    assertEquals(0.945, i.matchAccuracy.get(), 0.01f);
    assertEquals(MatchType.NAME_AND_YEAR, i.matchType.get());
    assertEquals(new ProviderId("Movie", "TMDB", "80000"), i.providerId.get());
  }

  @Test
  public void shouldIdentifyMovies2() throws JsonProcessingException, IOException {
    when(tmdb.query(eq("3/search/movie"), eq("query"), eq("Michiel de Ruyter"), eq("language"), eq("en"))).thenReturn(objectMapper.readTree("{\"results\":[{\"id\":80001,\"original_title\":\"Michiel de Ruyter\",\"release_date\":\"2015-07-01\",\"title\":\"Admiral\"}]}"));

    movie.initialTitle.set("Michiel de Ruyter");
    movie.localReleaseYear.set("2015");

    Identifier i = identifier.identify(movie);

    assertEquals(1.0, i.matchAccuracy.get(), 0.01f);
    assertEquals(MatchType.NAME_AND_YEAR, i.matchType.get());
    assertEquals(new ProviderId("Movie", "TMDB", "80001"), i.providerId.get());
  }

  @Test
  public void shouldNotFailWhenNoOriginalTitlePresent() throws JsonProcessingException, IOException {
    when(tmdb.query(eq("3/search/movie"), eq("query"), eq("Michiel de Ruyter"), eq("language"), eq("en"))).thenReturn(objectMapper.readTree("{\"results\":[{\"id\":80001,\"release_date\":\"2015-07-01\",\"title\":\"Admiral\"}]}"));

    movie.initialTitle.set("Michiel de Ruyter");
    movie.localReleaseYear.set("2015");

    Identifier i = identifier.identify(movie);

    assertEquals(0.515, i.matchAccuracy.get(), 0.01f);
    assertEquals(MatchType.NAME_AND_YEAR, i.matchType.get());
    assertEquals(new ProviderId("Movie", "TMDB", "80001"), i.providerId.get());
  }

  @Test
  public void shouldIdentifyByImdbId() throws JsonProcessingException, IOException {
    when(tmdb.query(eq("3/find/12345"), eq("external_source"), eq("imdb_id"))).thenReturn(objectMapper.readTree("{\"movie_results\":[{\"id\":80001,\"release_date\":\"2015-07-01\",\"title\":\"Admiral\"}]}"));

    movie.initialTitle.set("Michiel de Ruyter");
    movie.localReleaseYear.set("2015");
    movie.initialImdbNumber.set("12345");

    Identifier i = identifier.identify(movie);

    assertEquals(1.0, i.matchAccuracy.get(), 0.01f);
    assertEquals(MatchType.ID, i.matchType.get());
    assertEquals(new ProviderId("Movie", "TMDB", "80001"), i.providerId.get());
  }

  @Test
  public void createVariationsShouldCreateCorrectVariations() {
    assertEquals(Arrays.asList("2012"), identifier.createVariations("2012"));
    assertEquals(Arrays.asList("I, Robot", "Robot I", "I"), identifier.createVariations("I, Robot"));
    assertEquals(Arrays.asList("Terminator, The", "The Terminator", "Terminator"), identifier.createVariations("Terminator, The"));
    assertEquals(Arrays.asList("Michiel de Ruyter", "Admiral, The", "The Admiral", "Admiral"), identifier.createVariations("Michiel de Ruyter (Admiral, The)"));
  }
}
