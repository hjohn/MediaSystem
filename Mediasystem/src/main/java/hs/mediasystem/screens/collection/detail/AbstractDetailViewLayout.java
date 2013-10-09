package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.screens.Layout;
import hs.mediasystem.util.javafx.Listeners;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;

/**
 * Abstract base class for Layouts to be used with {@link DetailView}.  It provides
 * boilerplate for binding the content property and setting up the navigation event.
 *
 * @param <C> class of supported content
 */
public abstract class AbstractDetailViewLayout<C> implements Layout<C, DetailPanePresentation> {

  /**
   * Implement to provide a DetailPane instance which can handle C content.
   *
   * @param presentation a DetailPanePresentation
   * @return a DetailPane instance which can handle C content
   */
  protected abstract DetailPane<C> createDetailPane(DetailPanePresentation presentation);

  @Override
  public DetailPanePresentation createPresentation() {
    return new DetailPanePresentation();
  }

  @Override
  public final Node createView(DetailPanePresentation presentation) {
    DetailPane<C> detailPane = createDetailPane(presentation);

    Listeners.bind(presentation.content, new ChangeListener<Object>() {
      @SuppressWarnings("unchecked")
      @Override
      public void changed(ObservableValue<? extends Object> observableValue, Object old, Object current) {
        detailPane.content.set((C)(getContentClass().isInstance(current) ? current : null));
      }
    });

    detailPane.onNavigate.set(new EventHandler<DetailNavigationEvent>() {
      @Override
      public void handle(DetailNavigationEvent event) {
        presentation.content.set(event.getContent());
      }
    });

    return detailPane;
  }
}
