package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.DatabaseUrlSource;
import hs.mediasystem.dao.Source;
import hs.mediasystem.db.Database;
import hs.mediasystem.framework.Cache;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class TheMovieDatabase {
  static final Executor EXECUTOR = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>());

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final RateLimiter RATE_LIMITER = new RateLimiter(15, 10);

  private final String apiKey;
  private final Cache cache;
  private final Database database;
  private final ObjectMapper objectMapper = new ObjectMapper();

  private JsonNode configuration;

  @Inject
  public TheMovieDatabase(Cache cache, Database database) {  // TODO we have Cache, no need for Database
    this.cache = cache;
    this.database = database;
    this.apiKey = CryptoUtil.decrypt("8AF22323DB8C0F235B38F578B7E09A61DB6F971EED59DE131E4EF70003CE84B483A778EBD28200A031F035F4209B61A4", "-MediaSystem-");
  }

  public JsonNode query(String query, String... parameters) {
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

      return objectMapper.readTree(getURL(new URL("http://api.themoviedb.org/" + query + "?api_key=" + apiKey + sb.toString())));
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
      configuration = query("3/configuration");
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
    byte[] data = cache.lookup(url.toExternalForm());

    if(data == null) {
      RATE_LIMITER.acquire();
      data = URLs.readAllBytes(url);

      System.out.println("[FINE] [TMDB] [CACHE] Store: " + url);
      cache.store(url.toExternalForm(), data);
    }

    return data;
  }
}
