package hs.mediasystem.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.framework.Identifier;
import hs.mediasystem.framework.MediaData;
import hs.mediasystem.test.JavaFXRunningRule;

import org.junit.Rule;
import org.junit.Test;

public class EntityTest {

  @Rule
  public final JavaFXRunningRule jfxRunningRule = new JavaFXRunningRule();

  @Test
  public void shouldEnrichEntityWhenAccessed() throws InterruptedException {
    final Identifier identifier = new Identifier(new ProviderId("Movie", "TMDB", "1"), MatchType.ID, 1.0f);

    identifier.mediaData.setEnricher(new EnricherBuilder<Identifier, MediaData>(MediaData.class)
      .enrich(new EnrichCallback<MediaData>() {
        @Override
        public MediaData enrich(Object... parameters) {
          return new MediaData("http://somewhere", 1000, null, 0, false);
        }
      })
      .finish(new FinishEnrichCallback<MediaData>() {
        @Override
        public void update(MediaData result) {
          identifier.mediaData.set(result);
        }
      })
      .build()
    );

    assertNull(identifier.mediaData.get());

    Thread.sleep(500);

    assertNotNull(identifier.mediaData.get());
    assertEquals(1000L, identifier.mediaData.get().fileLength.get());
    assertEquals("http://somewhere", identifier.mediaData.get().uri.get());
  }
}
