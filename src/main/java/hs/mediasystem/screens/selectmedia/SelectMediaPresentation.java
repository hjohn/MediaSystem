package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.screens.MediaItemEnrichmentEventHandler;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.Navigator;
import hs.mediasystem.screens.SelectMediaView;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.StandardLayout;
import hs.mediasystem.screens.optiondialog.ActionOption;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.StateCache;
import hs.mediasystem.util.StringBinding;

import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javax.inject.Inject;

public class SelectMediaPresentation {
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);
  private final SelectMediaView view;
  private final Navigator navigator;
  private final StandardLayout layout = new StandardLayout();
  private final StateCache stateCache;

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaView view, final MediaItemEnrichmentEventHandler enrichmentHandler, final StateCache stateCache) {
    this.stateCache = stateCache;
    this.navigator = new Navigator(controller.getNavigator());
    this.view = view;

    view.onBack().set(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        navigator.back();
        event.consume();
      }
    });

    view.onNodeSelected().set(new EventHandler<MediaNodeEvent>() {
      @Override
      public void handle(MediaNodeEvent event) {
        if(event.getMediaNode().getMediaItem().isLeaf()) {
          controller.play(event.getMediaNode().getMediaItem());
        }
        else {
          setTreeRoot(event.getMediaNode().getMediaItem()); // TODO Could trigger a new pane altogether
        }
        event.consume();
      }
    });

    view.onNodeAlternateSelect().set(new EventHandler<MediaNodeEvent>() {
      @Override
      public void handle(MediaNodeEvent event) {
        final MediaItem mediaItem = event.getMediaNode().getMediaItem();
        List<? extends Option> options = FXCollections.observableArrayList(
          new ActionOption("Reload meta data", new Callable<Boolean>() {
            @Override
            public Boolean call() {
              enrichmentHandler.enrich(mediaItem, true);

              ImageCache.expunge(mediaItem.getBanner());
              ImageCache.expunge(mediaItem.getPoster());
              ImageCache.expunge(mediaItem.getBackground());
              return true;
            }
          })
        );

        controller.showOptionScreen("Options: " + mediaItem.getTitle(), options);
        event.consume();
      }
    });

    getView().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(KEY_O.match(event)) {
          @SuppressWarnings("unchecked")
          List<? extends Option> options = FXCollections.observableArrayList(
            new ListOption<>("Group by", layout.groupSetProperty(), layout.availableGroupSetsProperty(), new StringBinding(layout.groupSetProperty()) {
              @Override
              protected String computeValue() {
                return layout.groupSetProperty().get().getTitle();
              }
            }),
            new ListOption<>("Order by", layout.sortOrderProperty(), layout.availableSortOrdersProperty(), new StringBinding(layout.sortOrderProperty()) {
              @Override
              protected String computeValue() {
                return layout.sortOrderProperty().get().getTitle();
              }
            })
          );

          controller.showOptionScreen("Media - Options", options);
          event.consume();
        }
      }
    });

    layout.sortOrderProperty().addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        view.setRoot(layout.wrap(currentRoot));
      }
    });
  }

  public Node getView() {
    return (Node)view;
  }

  private MediaItem currentRoot;

  private void setTreeRoot(final MediaItem root) {
    currentRoot = root;
    navigator.navigateTo(new Destination(root.getTitle()) {
      @Override
      public void execute() {
        MediaNode mediaNode = layout.wrap(root);

        view.setRoot(mediaNode);

        String key = createKeyFromTrail();
        String id = stateCache.getState(key);
        MediaNode nodeToSelect = null;

        if(id != null) {
          nodeToSelect = mediaNode.findMediaNode(id);
        }

        view.setSelectedNode(nodeToSelect);
      }

      @Override
      protected void outro() {
        MediaNode selectedNode = view.getSelectedNode();

        if(selectedNode != null) {
          stateCache.putState(createKeyFromTrail(), selectedNode.getId());
        }
      }

      private String createKeyFromTrail() {
        String key = "";

        for(Destination destination : navigator.getTrail()) {
          if(!key.isEmpty()) {
            key += ";";
          }
          key += destination.getDescription();
        }

        return key;
      }
    });
  }

  public void setMediaTree(final MediaTree mediaTree) {
    setTreeRoot(mediaTree.getRoot());
  }
}
