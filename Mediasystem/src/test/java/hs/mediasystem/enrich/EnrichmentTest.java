package hs.mediasystem.enrich;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.dao.MediaId;
import hs.mediasystem.enrich.EnrichCache.CacheKey;
import hs.mediasystem.framework.MediaDataEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemUri;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.test.JavaFXTestCase;
import hs.mediasystem.util.TaskThreadPoolExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EnrichmentTest extends JavaFXTestCase {
  private static final String MOVIE_ALICE_URI = "";

  private MediaTree mediaTree;
  private MediaItem item;
  private CacheKey cacheKey;
  private EnrichCache cache;
  private List<SlowData> slowDataObjects;

  @Mock private MediaDataDao mediaDataDao;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    slowDataObjects = new ArrayList<>();

    cache = new EnrichCache(new TaskThreadPoolExecutor(new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())));
    cache.registerEnricher(MediaData.class, new MediaDataEnricher(mediaDataDao));
    cache.registerEnricher(ExtraMediaData.class, new Enricher<ExtraMediaData>() {
      @Override
      public List<EnrichTask<ExtraMediaData>> enrich(final Parameters parameters, boolean bypassCache) {
        return new ArrayList<EnrichTask<ExtraMediaData>>() {{
          add(new EnrichTask<ExtraMediaData>(true) {
            @Override
            protected ExtraMediaData call() throws Exception {
              return null;
            }
          });
          add(new EnrichTask<ExtraMediaData>(true) {
            @Override
            protected ExtraMediaData call() throws Exception {
              return null;
            }
          });
          add(new EnrichTask<ExtraMediaData>(false) {
            @Override
            protected ExtraMediaData call() throws Exception {
              return new ExtraMediaData();
            }
          });
        }};
      }

      @Override
      public List<Class<?>> getInputTypes() {
        return new ArrayList<Class<?>>() {{
          add(MediaData.class);
        }};
      }
    });

    cache.registerEnricher(FailMediaData.class, new Enricher<FailMediaData>() {
      @Override
      public List<EnrichTask<FailMediaData>> enrich(final Parameters parameters, boolean bypassCache) {
        return new ArrayList<EnrichTask<FailMediaData>>() {{
          add(new EnrichTask<FailMediaData>(true) {
            @Override
            protected FailMediaData call() throws Exception {
              return null;
            }
          });
          add(new EnrichTask<FailMediaData>(true) {
            @Override
            protected FailMediaData call() throws Exception {
              throw new RuntimeException("failed");
            }
          });
        }};
      }

      @Override
      public List<Class<?>> getInputTypes() {
        return new ArrayList<Class<?>>() {{
          add(MediaData.class);
        }};
      }
    });

    cache.registerEnricher(ExtraFailMediaData.class, new Enricher<ExtraFailMediaData>() {
      @Override
      public List<EnrichTask<ExtraFailMediaData>> enrich(final Parameters parameters, boolean bypassCache) {
        return new ArrayList<EnrichTask<ExtraFailMediaData>>() {{
          add(new EnrichTask<ExtraFailMediaData>(true) {
            @Override
            protected ExtraFailMediaData call() throws Exception {
              return new ExtraFailMediaData();
            }
          });
        }};
      }

      @Override
      public List<Class<?>> getInputTypes() {
        return new ArrayList<Class<?>>() {{
          add(FailMediaData.class);
        }};
      }
    });

    cache.registerEnricher(SlowData.class, new Enricher<SlowData>() {
      @Override
      public List<EnrichTask<SlowData>> enrich(final Parameters parameters, boolean bypassCache) {
        return new ArrayList<EnrichTask<SlowData>>() {{
          add(new EnrichTask<SlowData>(true) {
            @Override
            protected SlowData call() throws Exception {
              Thread.sleep(100);
              SlowData slowData = new SlowData(parameters.unwrap(MediaItemUri.class));
              synchronized(slowDataObjects) {
                slowDataObjects.add(slowData);
              }
              return slowData;
            }
          });
        }};
      }

      @Override
      public List<Class<?>> getInputTypes() {
        return new ArrayList<Class<?>>() {{
          add(MediaItemUri.class);
        }};
      }
    });

    mediaTree = new MediaTree() {
      @Override
      public EnrichCache getEnrichCache() {
        return cache;
      }

      @Override
      public PersistQueue getPersister() {
        return null;
      }
    };

    TestMovie movie = new TestMovie("Alice in Wonderland", null, null, null, null);
    item = new MediaItem(mediaTree, MOVIE_ALICE_URI, movie);
    cacheKey = new CacheKey(item.getUri());
  }

  @Test
  public void shouldUpdateEnrichableWhenCacheUpdated() {
    cache.insert(cacheKey, new MediaId(1, 0, 0, null, null));

    sleep(150);

    assertNotNull(item.get(MediaId.class));
  }

  @Test
  public void shouldImmediatelyFailEnrichmentWhenNoSuitableEnricherExists() {
    assertNull(item.get(TestMovie.class).getTagLine());  // triggers enrich
    assertEquals(EnrichmentState.FAILED, item.getEnrichmentState(TestMovie.class));
  }

  @Test
  public void shouldEnrichDependenciesFirst() {
    assertNull(item.get(ExtraMediaData.class));

    sleep(150);

    assertNotNull(item.get(MediaData.class));  // dependency now present
    assertNotNull(item.get(ExtraMediaData.class));
  }

  @Test
  public void shouldFailOnEnrichException() {
    assertNull(item.get(FailMediaData.class));

    sleep(150);

    assertEquals(EnrichmentState.FAILED, item.getEnrichmentState(FailMediaData.class));  // fails due to exception
  }

  @Test
  public void shouldFailOnDependencyEnrichException() {
    assertNull(item.get(ExtraFailMediaData.class));

    sleep(150);

    assertEquals(EnrichmentState.FAILED, item.getEnrichmentState(FailMediaData.class));  // dependency failed
    assertEquals(EnrichmentState.FAILED, item.getEnrichmentState(ExtraFailMediaData.class));  // so this failed too
  }

  @Test(timeout = 5000)
  public void shouldEnrichIdentifierWhenRequested() {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        assertNull(item.get(MediaData.class));
      }
    });

    sleep(150);

    assertNotNull(item.get(MediaData.class));
  }

  @Test(timeout = 10000)
  public void shouldPromoteMostRecentlyRequestedData() throws InterruptedException {
    final int total = 100;
    TestMovie movie = new TestMovie("Bakerstreet", null, null, null, null);
    List<MediaItem> mediaItems = new ArrayList<>();

    for(int i = 0; i < total; i++) {
      mediaItems.add(new MediaItem(mediaTree, "item-" + i, movie));
    }

    /*
     * Trigger all enrich events, these will be processed by default in FIFO order
     */

    for(MediaItem mediaItem : mediaItems) {
      mediaItem.get(SlowData.class);
    }

    mediaItems.get(25).get(SlowData.class);

    /*
     * Wait for all enrichments to complete
     */

    while(slowDataObjects.size() != total) {
      Thread.sleep(50);
    }

    /*
     * Generally the order is expected to be something like the first few tasks in random order (depending on number of threads and when they execute),
     * followed by (mostly) in order the highest task to the lowest task.
     *
     * By accessing number 25 immediately after all others are triggered, it must also be executed somewhere in the first few tasks (say as one of
     * the first 10), instead of normally as one of the last 25 tasks (index 75+).
     */

    assertTrue(slowDataObjects.indexOf(new SlowData("item-25")) < 10);
  }

  private static class SlowData extends DefaultEnrichable<SlowData> {
    private final String uri;

    public SlowData(String uri) {
      this.uri = uri;
    }

    @Override
    public int hashCode() {
      return uri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return uri.equals(((SlowData)obj).uri);
    }
  }

  private static class ExtraMediaData {
  }

  private static class FailMediaData {
  }

  private static class ExtraFailMediaData {
  }
}
