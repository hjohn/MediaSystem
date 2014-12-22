package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.PresentationPane;
import hs.mediasystem.screens.UserLayout;
import hs.mediasystem.util.GridPaneUtil;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * The CollectionView provides a standard area with a background and a small bottom border in which
 * information about a collection of media can be shown.  How the collection is shown is determined by
 * the current layout.<p>
 *
 * Provided properties, events and methods:
 * - the MediaRoot under which the media items to be displayed are located
 * - the currently focused media item
 * - the layout to use
 * - the group set to use
 * - an event handler triggered when a node is selected
 * - an event handler triggered when options is chosen
 * - a method for selecting a node by id
 */
public class CollectionView extends PresentationPane {

  /**
   * The MediaNodes to display.
   */
  public final ObservableList<MediaNode> mediaNodes = FXCollections.observableArrayList();

  /**
   * Whether or not the top level of MediaNodes should be displayed in expanded form.
   */
  public final BooleanProperty expandTopLevel = new SimpleBooleanProperty();

  /**
   * The currently focused media node.
   */
  public final ObjectProperty<MediaNode> focusedMediaNode = new SimpleObjectProperty<>();

  /**
   * The current active layout.
   */
  public final ObjectProperty<UserLayout<MediaRoot, CollectionSelectorPresentation>> layout = new SimpleObjectProperty<>();

  /**
   * Triggered when a MediaNode is selected.
   */
  public final ObjectProperty<EventHandler<MediaNodeEvent>> onSelect = new SimpleObjectProperty<>();

  private final BackgroundPane backgroundPane = new BackgroundPane();
  private final StackPane collectionSelectorPane = new StackPane();

  private CollectionSelectorPresentation currentCollectionSelectorPresentation;

  private boolean focusValid;

  public CollectionView() {
    getStylesheets().add("collection/collection-view.css");

    GridPane stage = GridPaneUtil.create(new double[] {100}, new double[] {90, 10});

    stage.add(backgroundPane, 0, 0);
    stage.add(new StackPane() {{
      getStyleClass().add("stage");
    }}, 0, 1);

    getChildren().addAll(stage, collectionSelectorPane);

    backgroundPane.mediaNodeProperty().bind(focusedMediaNode);

    /*
     * Listener for layout changes.  When the layout changes a new child presentation must be bound to this
     * view.  This presentation is then used in turn together with the layout to construct a new child view
     * that is set as part of this view.
     */

    layout.addListener(new ChangeListener<Layout<MediaRoot, CollectionSelectorPresentation>>() {
      @Override
      public void changed(ObservableValue<? extends Layout<MediaRoot, CollectionSelectorPresentation>> observableValue, Layout<MediaRoot, CollectionSelectorPresentation> oldLayout, Layout<MediaRoot, CollectionSelectorPresentation> layout) {
        if(currentCollectionSelectorPresentation != null) {
          currentCollectionSelectorPresentation.focusedMediaNode.unbindBidirectional(focusedMediaNode);
          currentCollectionSelectorPresentation.expandTopLevel.unbindBidirectional(expandTopLevel);
          currentCollectionSelectorPresentation.onSelect.unbindBidirectional(onSelect);

          Bindings.unbindContent(currentCollectionSelectorPresentation.mediaNodes, mediaNodes);
        }

        if(layout != null) {
          currentCollectionSelectorPresentation = layout.createPresentation();

          Bindings.bindContent(currentCollectionSelectorPresentation.mediaNodes, mediaNodes);

          currentCollectionSelectorPresentation.focusedMediaNode.bindBidirectional(focusedMediaNode);
          currentCollectionSelectorPresentation.expandTopLevel.bindBidirectional(expandTopLevel);
          currentCollectionSelectorPresentation.onSelect.bindBidirectional(onSelect);

          collectionSelectorPane.getChildren().setAll(layout.createView(currentCollectionSelectorPresentation));
        }
      }
    });

    /*
     * Listener for when the collection of MediaNodes changes.  When this happens, the currently
     * focused MediaNode may not be the same instance anymore as the same node in the new
     * collection.  If it still exists however it can be reselected by id in the next layout pass.
     */

    mediaNodes.addListener((Observable o) -> {
      if(focusValid) {
        focusValid = false;
        requestLayout();
      }
    });
  }

  @Override
  public Object getPresentation() {
    return currentCollectionSelectorPresentation;
  }

  @Override
  public void requestFocus() {
    if(currentCollectionSelectorPresentation != null) {
      currentCollectionSelectorPresentation.defaultInputFocus.get().requestFocus();
    }
  }

  @Override
  protected void layoutChildren() {
    super.layoutChildren();

    if(!focusValid) {
      String id = null;

      if(focusedMediaNode.get() != null) {
        id = focusedMediaNode.get().getId();
      }

      selectMediaNodeById(id);
      focusValid = true;
    }
  }

  private void selectMediaNodeById(String id) {
    MediaNode nodeToSelect = null;

    if(id != null) {
      for(MediaNode mediaNode : mediaNodes) {
        nodeToSelect = mediaNode.findMediaNodeById(id);

        if(nodeToSelect != null) {
          break;
        }
      }
    }

    if(nodeToSelect == null && !mediaNodes.isEmpty()) {
      nodeToSelect = mediaNodes.get(0);
    }

    focusedMediaNode.set(nodeToSelect);
  }
}
