package hs.mediasystem.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import hs.mediasystem.db.EnricherMatch;
import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.MediaData.MatchType;
import hs.mediasystem.db.MediaId;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.CacheKey;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.enrich.EnrichmentState;
import hs.mediasystem.framework.MediaDataEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.util.TaskThreadPoolExecutor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EnrichmentTest {
  private static final String MOVIE_ALICE_URI = "";

  private MediaTree mediaTree;
  private MediaItem item;
  private CacheKey cacheKey;
  private EnrichCache cache;

  @Mock private ItemsDao itemsDao;
  @Mock private TypeBasedItemEnricher typeBasedItemEnricher;

  @BeforeClass
  public static void beforeClass() {
    new Thread() {
      @Override
      public void run() {
        Application.launch(FXRunnerApplication.class);
      }
    }.start();
  }

  @Before
  public void before() throws IdentifyException {
    MockitoAnnotations.initMocks(this);

    cache = new EnrichCache(new TaskThreadPoolExecutor(new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())));
    cache.registerEnricher(MediaData.class, new MediaDataEnricher(itemsDao, typeBasedItemEnricher));

    mediaTree = new MediaTree() {
      @Override
      public EnrichCache getEnrichCache() {
        return cache;
      }
    };

    TestMovie movie = new TestMovie("Alice in Wonderland", null, null, null, null);
    item = new MediaItem(mediaTree, MOVIE_ALICE_URI, movie);
    cacheKey = new CacheKey(item.getUri());

    when(typeBasedItemEnricher.identifyItem(movie)).thenReturn(new EnricherMatch(new Identifier("Movie", "TMDB", "12345"), MatchType.NAME, 0.99f));
  }

  @Test
  public void shouldUpdateEnrichableWhenCacheUpdated() {
    cache.insert(cacheKey, new MediaId(1, 0, null, null));

    assertNotNull(item.get(MediaId.class));
  }

  @Test
  public void shouldImmediatelyFailEnrichmentWhenNoSuitableEnricherExists() {
    assertNull(item.get(TestMovie.class).getTagLine());  // triggers enrich
    assertEquals(EnrichmentState.FAILED, item.getEnrichmentState(TestMovie.class));
  }

  @Test(timeout = 5000)
  public void shouldEnrichMediaDataWhenRequested() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        assertNull(item.get(MediaData.class));
      }
    });

    sleep(150);

    assertNotNull(item.get(MediaData.class));
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    }
    catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static class FXRunnerApplication extends Application {
    @Override
    public void start(Stage paramStage) throws Exception {
    }
  }
}
