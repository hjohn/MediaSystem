package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.PresentationPane;
import hs.mediasystem.screens.UserLayout;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.GridPaneUtil;

import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);

  /**
   * The collection root under which the media items to be displayed are located.
   */
  public final ObjectProperty<MediaRoot> mediaRoot = new SimpleObjectProperty<>();

  /**
   * The currently focused media node.
   */
  public final ObjectProperty<MediaNode> focusedMediaNode = new SimpleObjectProperty<>();

  /**
   * The current active layout.
   */
  public final ObjectProperty<UserLayout<MediaRoot, CollectionSelectorPresentation>> layout = new SimpleObjectProperty<>();

  /**
   * The current active group set.
   */
  public final ObjectProperty<MediaGroup> groupSet = new SimpleObjectProperty<>();

  /**
   * Triggered when a MediaNode is selected.
   */
  public final ObjectProperty<EventHandler<MediaNodeEvent>> onSelect = new SimpleObjectProperty<>();

  /**
   * Triggered when options is chosen.
   */
  public final ObjectProperty<EventHandler<ActionEvent>> onOptionsSelect = new SimpleObjectProperty<>();

  private final BackgroundPane backgroundPane = new BackgroundPane();
  private final StackPane collectionSelectorPane = new StackPane();
  private final ObjectProperty<MediaNode> rootMediaNode = new SimpleObjectProperty<>();

  private CollectionSelectorPresentation currentCollectionSelectorPresentation;

  public CollectionView() {
    getStylesheets().add("collection/collection-view.css");

    GridPane stage = GridPaneUtil.create(new double[] {100}, new double[] {90, 10});

    stage.add(backgroundPane, 0, 0);
    stage.add(new StackPane() {{
      getStyleClass().add("stage");
    }}, 0, 1);

    getChildren().addAll(stage, collectionSelectorPane);

    addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(KEY_O.match(event)) {
          Events.dispatchEvent(onOptionsSelect, new ActionEvent(), event);
        }
      }
    });

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
          currentCollectionSelectorPresentation.onSelect.unbindBidirectional(onSelect);
          currentCollectionSelectorPresentation.rootMediaNode.unbindBidirectional(rootMediaNode);
        }

        if(layout != null) {
          currentCollectionSelectorPresentation = layout.createPresentation();

          currentCollectionSelectorPresentation.rootMediaNode.bindBidirectional(rootMediaNode);
          currentCollectionSelectorPresentation.focusedMediaNode.bindBidirectional(focusedMediaNode);
          currentCollectionSelectorPresentation.onSelect.bindBidirectional(onSelect);

          collectionSelectorPane.getChildren().setAll(layout.createView(currentCollectionSelectorPresentation));
        }
      }
    });

    /*
     * Listeners for changes that require rootMediaNode to be updated
     */

    InvalidationListener rootMediaNodeUpdater = new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        MediaGroup mediaGroup = groupSet.get();

        if(mediaGroup != null && mediaRoot.get() != null) {
          // TODO RootNode is a stupid concept, there is really no root node needed -- items should just be an obervablelist -- this will simplify MediaNode code as well
          MediaNode root = new MediaNode(mediaRoot.get(), null, mediaGroup.showTopLevelExpanded(), false, mediaGroup);

          rootMediaNode.set(root);
        }
      }
    };

    mediaRoot.addListener(rootMediaNodeUpdater);
    groupSet.addListener(rootMediaNodeUpdater);
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

  public void selectMediaNodeById(String id) {
    MediaNode nodeToSelect = null;

    if(id != null) {
      nodeToSelect = rootMediaNode.get().findMediaNode(id);
    }

    if(nodeToSelect == null) {
      List<MediaNode> children = rootMediaNode.get().getChildren();

      if(!children.isEmpty()) {
        if(groupSet.get().showTopLevelExpanded()) {
          if(!children.get(0).getChildren().isEmpty()) {
            nodeToSelect = children.get(0).getChildren().get(0);
          }
        }
        else {
          nodeToSelect = children.get(0);
        }
      }
    }

    focusedMediaNode.set(nodeToSelect);
  }
}
