package hs.mediasystem.screens.collection.detail;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hs.mediasystem.entity.Movie;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.test.JavaFXRunningRule;

import java.util.HashSet;

import javafx.scene.control.Button;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DetailPaneViewTest {
  private DetailView view;

  @Rule
  public final JavaFXRunningRule jfxRunningRule = new JavaFXRunningRule();

  @Mock
  private Layout<Object, DetailPanePresentation> layout;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);

    Mockito.<Class<?>>when(layout.getContentClass()).thenReturn(Object.class);
    when(layout.create(any(DetailPanePresentation.class))).thenReturn(new Button());

    this.view = new DetailView(
      new HashSet<Layout<? extends Object, DetailPanePresentation>>() {{
        add(layout);
      }},
      false,
      null
    );
  }

  @Test
  public void shouldUpdateMediaAfterIdentification() {
    MediaItem mediaItem = new MediaItem("", "Some Title", Movie.class);

    /*
     * Check if setting the content results in a new layout being created:
     */

    view.content.set(new MediaNode(mediaItem));

    verify(layout, times(1)).create(any(DetailPanePresentation.class));

    /*
     * Check if changing media also results in a new layout being created:
     */

    mediaItem.media.set(new Media<MediaNode.SpecialItem>());

    verify(layout, times(2)).create(any(DetailPanePresentation.class));
  }
}
