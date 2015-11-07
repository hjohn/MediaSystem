package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.DatabaseUrlSource;
import hs.mediasystem.dao.Source;
import hs.mediasystem.db.Database;
import hs.mediasystem.framework.Cache;
import hs.mediasystem.framework.CacheEntry;
import hs.mediasystem.util.CryptoUtil;
import hs.mediasystem.util.LifoBlockingDeque;
import hs.mediasystem.util.RateLimiter;
import hs.mediasystem.util.io.RuntimeIOException;
import hs.mediasystem.util.io.URLs;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class TheMovieDatabase {

  /**
   * Executor used for priority fetching of TMDB data
   */
  static final Executor EXECUTOR = new ThreadPoolExecutor(3, 3, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>());

  /**
   * Executor used for background refreshing of TMDB data
   */
  private static final Executor BACKGROUND_REFRESH_EXECUTOR = new ThreadPoolExecutor(1, 1, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>());

  private static final Logger LOGGER = Logger.getLogger(TheMovieDatabase.class.getName());
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * TMDB allows a maximum of 30 queries in a period of 10 seconds, this rate limiter allows 15 queries per 10 seconds.
   */
  private static final RateLimiter GLOBAL_RATE_LIMITER = new RateLimiter(15, 10);

  /**
   * For background refreshing we limit the rate to 2 queries per 10 seconds.  Setting this lower than the global limiter
   * will ensure that background refreshes do not drown out normal fetches.
   */
  private static final RateLimiter BACKGROUND_REFRESH_RATE_LIMITER = new RateLimiter(2, 10);

  private final String apiKey;
  private final Cache<byte[]> cache;
  private final Database database;
  private final int maxAgeInSeconds;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private JsonNode configuration;

  @Inject
  public TheMovieDatabase(Cache<byte[]> cache, Database database, @Named("TheMovieDatabase.expirationSeconds") int maxAgeInSeconds) {  // TODO we have Cache, no need for Database
    this.cache = cache;
    this.database = database;
    this.maxAgeInSeconds = maxAgeInSeconds;
    this.apiKey = CryptoUtil.decrypt("8AF22323DB8C0F235B38F578B7E09A61DB6F971EED59DE131E4EF70003CE84B483A778EBD28200A031F035F4209B61A4", "-MediaSystem-"); // Yes, I know you can still get the key.
  }

  public JsonNode query(String query, String... parameters) {
    return queryInternal(true, query, parameters);
  }

  private JsonNode queryInternal(boolean useCache, String query, String... parameters) {
    if(parameters.length % 2 != 0) {
      throw new IllegalArgumentException("Uneven number of vararg 'parameters': must provide pairs of name/value");
    }

    try {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < parameters.length; i += 2) {
        sb.append("&");
        sb.append(parameters[i]);
        sb.append("=");
        sb.append(URLEncoder.encode(parameters[i + 1], "UTF-8"));
      }

      LOGGER.info("Querying TMDB: " + query + " with parameters: " + Arrays.toString(parameters));

      URL url = new URL("http://api.themoviedb.org/" + query + "?api_key=" + apiKey + sb.toString());

      return objectMapper.readTree(useCache ? getURL(url) : getURLdirect(url));
    }
    catch(RuntimeIOException | IOException e) {
      throw new RuntimeException("While executing query: " + query + "; parameters=" + Arrays.toString(parameters), e);
    }
  }

  public static LocalDate parseDateOrNull(String text) {
    try {
      return text == null ? null : DATE_TIME_FORMATTER.parse(text, new TemporalQuery<LocalDate>() {
        @Override
        public LocalDate queryFrom(TemporalAccessor temporal) {
          return LocalDate.from(temporal);
        }
      });
    }
    catch(DateTimeParseException e) {
      return null;
    }
  }

  public String createImageURL(String path, String size) {
    if(path == null || size == null) {
      return null;
    }

    return getConfiguration().get("images").get("base_url").asText() + size + path;
  }

  private JsonNode getConfiguration() {
    if(configuration == null) {
      configuration = queryInternal(false, "3/configuration");
    }

    return configuration;
  }

  public Source<byte[]> createSource(String url) {
    if(url == null) {
      return null;
    }

    try {
      return DatabaseUrlSource.create(database, url);
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private byte[] getURL(URL url) {
    CacheEntry<byte[]> entry = cache.lookup(url.toExternalForm());

    if(entry == null) {
      // No cache, do a foreground fetch of the data now:
      byte[] data = getURLdirect(url);

      System.out.println("[FINE] [TMDB] [CACHE] Store: " + url);
      cache.store(url.toExternalForm(), data);

      return data;
    }

    /*
     * Entry was cached, do a background refresh if needed.  Entries are refreshed if they exceed maxAgeInSeconds
     * plus a random number of seconds between 0 and 20%.  The randomization should help to reduce the number of
     * expirations happening all at once.
     */
    LocalDateTime oldestAllowed = LocalDateTime.now().minusSeconds(maxAgeInSeconds).minusSeconds((int)(Math.random() * maxAgeInSeconds / 5));

    if(entry.getCreationTime().isBefore(oldestAllowed)) {  // entry is not fresh enough?
      // Trigger background refresh:
      BACKGROUND_REFRESH_EXECUTOR.execute(() -> {
        BACKGROUND_REFRESH_RATE_LIMITER.acquire();

        byte[] data = getURLdirect(url);

        System.out.println("[FINE] [TMDB] [CACHE] Background Store: " + url);
        cache.store(url.toExternalForm(), data);
      });
    }

    /*
     * Return the entry, whether is was fresh enough or not:
     */

    return entry.getData();
  }

  private static byte[] getURLdirect(URL url) {
    GLOBAL_RATE_LIMITER.acquire();

    return URLs.readAllBytes(url);
  }
}
