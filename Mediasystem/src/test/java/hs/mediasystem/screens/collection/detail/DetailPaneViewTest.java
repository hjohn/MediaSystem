package hs.mediasystem.screens.collection.detail;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    when(layout.createPresentation()).thenReturn(new DetailPanePresentation());
    when(layout.createView(any(DetailPanePresentation.class))).thenReturn(new Button());

    this.view = new DetailView(
      new HashSet<Layout<? extends Object, ? extends DetailPanePresentation>>() {{
        add(layout);
      }},
      false,
      null
    );
  }

  @Test
  public void shouldUpdateMediaAfterIdentification() {
    Media media = new Media(null, new MediaItem("")) {{
      initialTitle.set("Some Title");
    }};

    /*
     * Check if setting the content results in a new layout being created:
     */

    view.content.set(new MediaNode(media));

    verify(layout, times(1)).createView(any(DetailPanePresentation.class));
  }
}
