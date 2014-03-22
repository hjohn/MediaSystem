package hs.mediasystem.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import hs.mediasystem.dao.Source;
import hs.mediasystem.framework.SourceImageHandle;
import hs.mediasystem.test.JavaFXRunningRule;
import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.scene.image.Image;

import org.junit.Rule;
import org.junit.Test;

public class AsyncImagePropertyTest {
  @Rule
  public final JavaFXRunningRule jfxRunningRule = new JavaFXRunningRule();

  @Test
  public void shouldLoadImageInBackground() {
    AsyncImageProperty property = new AsyncImageProperty(50);

    property.imageHandleProperty().set(new SourceImageHandle(new FakeSource(), "image-key"));

    assertNull(property.get());
    sleep(200);
    assertNotNull(property.get());
  }

  @Test
  public void shouldClearImageImmediatelyWhenImageHandleDoesNotMatchImage() {
    AsyncImageProperty property = new AsyncImageProperty(50);

    property.imageHandleProperty().set(new SourceImageHandle(new FakeSource(), "image-key-A"));

    sleep(200);
    assertNotNull(property.get());

    property.imageHandleProperty().set(new SourceImageHandle(new FakeSource(), "image-key-B"));
    assertNull(property.get());
  }

  @Test
  public void shouldEndWithCorrectImageAfterMultipleChanges() {
    AsyncImageProperty property = new AsyncImageProperty(0);
    ImageHandle imageHandle = null;

    for(int i = 0; i < 100; i++) {
      imageHandle = new SourceImageHandle(new FakeSource(), "image-key-" + i);
      property.imageHandleProperty().set(imageHandle);

      sleep((int)(Math.random() * 50));

      Image image = property.get();

      assertTrue(image == null || image.equals(ImageCache.loadImageUptoMaxSize(imageHandle, 1920, 1200)));
    }

    sleep(200);

    assertEquals(ImageCache.loadImageUptoMaxSize(imageHandle, 1920, 1200), property.get());
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    }
    catch(InterruptedException e) {
    }
  }

  public static class FakeSource implements Source<byte[]> {
    @Override
    public byte[] get() {
      try {
        return Files.readAllBytes(Paths.get(getClass().getResource("aktion.png").toURI()));
      }
      catch(IOException | URISyntaxException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isLocal() {
      return true;
    }
  }
}
