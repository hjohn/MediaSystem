package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.AreaLayout;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.util.MapBindings;

import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Provides a view with details on the current (selected) content.<p>
 *
 * This class only provides the root part of the detail view; the actual
 * display of details is delegated to classes that are suited for the current
 * content by means of layouts.  Different layouts will be selected
 * dynamically depending on the available layout and the current content.
 */
public class DetailView extends StackPane {

  /**
   * Property with the content that should be displayed.
   */
  public final ObjectProperty<MediaNode> content = new SimpleObjectProperty<>();

  private final ObjectBinding<Media<?>> media = MapBindings.select(content, "media");
  private final ObjectProperty<Media<?>> derivedMedia = new SimpleObjectProperty<>();

  private final ObjectProperty<Object> finalContent = new SimpleObjectProperty<>();

  private final InvalidationListener finalContentUpdater = new InvalidationListener() {
    @Override
    public void invalidated(Observable observable) {
      finalContent.set(media.get() == null ? derivedMedia.get() : media.get());
    }
  };

  private DetailPanePresentation currentPresentation;

  /**
   * Constructs a new instance of this class.
   *
   * @param layouts a set of layouts this view is allowed to use
   * @param interactive whether the view allows user interaction
   * @param areaLayout an AreaLayout providing the base layout of the view area
   */
  public DetailView(Set<Layout<? extends Object, DetailPanePresentation>> layouts, boolean interactive, AreaLayout areaLayout) {

    /*
     * Change listener on content to update derivedMedia in case the MediaNode set in the content property is unidentified.
     */

    content.addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observableValue, MediaNode old, MediaNode current) {
        derivedMedia.set(current == null ? null : new MediaNode.SpecialItem(current.getId() + " (unidentified)"));
      }
    });

    /*
     * Listeners for media and derivedMedia to update the finalContent property.  These are triggered when the content
     * property changes (as both properties bind to the content property) and may result in the finalContent to change.
     */

    media.addListener(finalContentUpdater);
    derivedMedia.addListener(finalContentUpdater);

    /*
     * Change listener on finalContent to select an appropriate layout and update the UI.
     */

    finalContent.addListener(new ChangeListener<Object>() {
      @Override
      public void changed(ObservableValue<? extends Object> observableValue, Object old, Object current) {
        if(currentPresentation != null) {
          currentPresentation.content.unbindBidirectional(finalContent);
          currentPresentation = null;
        }

        if(current != null) {
          Layout<? extends Object, DetailPanePresentation> layout = Layout.findMostSuitableLayout(layouts, current.getClass());

          if(layout != null) {
            currentPresentation = new DetailPanePresentation(areaLayout, interactive);
            currentPresentation.content.bindBidirectional(finalContent);

            getChildren().setAll(layout.create(currentPresentation));
          }
          else {
            getChildren().setAll(new Label("Unable to display information for: " + current));
          }
        }
      }
    });
  }
}
