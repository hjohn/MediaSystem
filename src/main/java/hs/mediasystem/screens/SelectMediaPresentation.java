package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.SelectMediaView;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.ImageCache;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import javax.inject.Inject;

public class SelectMediaPresentation {
  private final SelectMediaView view;
  private final Navigator navigator;

  private MediaItem currentItem;
  private final StandardLayout layout = new StandardLayout();

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaView view, final MediaItemEnrichmentEventHandler enrichmentHandler) {
    this.navigator = new Navigator(controller.getNavigator());
    this.view = view;

    view.onBack().set(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        navigator.back();
        event.consume();
      }
    });

    view.onItemFocused().set(new EventHandler<TreeItemEvent<MediaItem>>() {
      @Override
      public void handle(TreeItemEvent<MediaItem> event) {
        if(event.getTreeItem() != null) {
          currentItem = event.getTreeItem().getValue();
          bind(event.getTreeItem().getValue());
        }
        event.consume();
      }
    });

    view.onItemSelected().set(new EventHandler<TreeItemEvent<MediaItem>>() {
      @Override
      public void handle(TreeItemEvent<MediaItem> event) {
        final MediaItem mediaItem = event.getTreeItem().getValue();
        System.out.println("[FINE] SelectMediaPresentation.SelectMediaPresentation(...).new EventHandler() {...}.handle() - item selected: " + mediaItem);

        if(mediaItem.isLeaf()) {
          controller.play(mediaItem);
        }
        else if(layout.isRoot(mediaItem)) {
          setTreeRoot(mediaItem);
        }
        else {
          event.getTreeItem().setExpanded(true);
        }
        event.consume();
      }
    });

    view.onItemAlternateSelect().set(new EventHandler<TreeItemEvent<MediaItem>>() {
      @Override
      public void handle(TreeItemEvent<MediaItem> event) {
        final MediaItem mediaItem = event.getTreeItem().getValue();

        List<? extends Option> options = FXCollections.observableArrayList(
          new ActionOption("Reload meta data", new Callable<Boolean>() {
            @Override
            public Boolean call() {
              enrichmentHandler.enrich(mediaItem, true);

              ImageCache.expunge(currentItem.getBanner());
              ImageCache.expunge(currentItem.getPoster());
              ImageCache.expunge(currentItem.getBackground());
              return true;
            }
          })
        );

        controller.showOptionScreen("Media - Options", options);
        event.consume();
      }
    });

    view.activeFilterItemProperty().addListener(new ChangeListener<Node>() {
      @Override
      public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node value) {
        Label oldLabel = (Label)oldValue;
        Label label = (Label)value;

        if(oldLabel != null) {
          oldLabel.setText(((FilterItem)oldValue.getUserData()).getShortText());
        }
        label.setText(((FilterItem)value.getUserData()).getLongText());

        refilter();
      }
    });
  }

  public Node getView() {
    return (Node)view;
  }

  private void setTreeRoot(final MediaItem root) {
    navigator.navigateTo(new Destination(root.getTitle()) {
      @Override
      public void execute() {
        TreeItem<MediaItem> treeRoot = new TreeItem<>(root);

        view.setCellFactory(new Callback<TreeView<MediaItem>, TreeCell<MediaItem>>() {
          @Override
          public TreeCell<MediaItem> call(TreeView<MediaItem> param) {
            return new MediaItemTreeCell(layout.getCellProvider(root));
          }
        });
        view.setRoot(treeRoot);
        view.filterItemsProperty().clear();

        List<FilterItem> filterItems = layout.getFilterItems(root);

        if(!filterItems.isEmpty()) {
          for(FilterItem item : filterItems) {
            Label label = new Label(item.getShortText());

            view.filterItemsProperty().add(label);
            label.setUserData(item);
          }

          view.activeFilterItemProperty().set(view.filterItemsProperty().get(0));
        }
        else {
          treeRoot.getChildren().clear();

          for(MediaItem item : layout.getChildren(root)) {
            treeRoot.getChildren().add(new MediaTreeItem(item));
          }
        }
      }
    });
  }

  public void setMediaTree(final MediaTree mediaTree) {
    setTreeRoot(mediaTree.getRoot());
  }

  private void refilter() {
    TreeItem<MediaItem> treeRoot = view.getRoot();

    treeRoot.getChildren().clear();

    FilterItem group = (FilterItem)view.activeFilterItemProperty().get().getUserData();

    for(MediaItem item : group.getMediaItem().children()) {
      treeRoot.getChildren().add(new MediaTreeItem(item));
    }
  }

  private void bind(final MediaItem mediaItem) {
    if(mediaItem != null) {
      view.mediaItemProperty().set(mediaItem);
    }
  }

  private final class MediaTreeItem extends TreeItem<MediaItem> {
    private boolean childrenPopulated;

    private MediaTreeItem(MediaItem value) {
      super(value);
    }

    @Override
    public boolean isLeaf() {
      return layout.isRoot(getValue()) || getValue().isLeaf();
    }

    @Override
    public ObservableList<TreeItem<MediaItem>> getChildren() {
      if(!childrenPopulated) {
        childrenPopulated = true;

        if(layout.hasChildren(getValue())) {
          for(MediaItem child : layout.getChildren(getValue())) {
            super.getChildren().add(new MediaTreeItem(child));
          }
        }
      }

      return super.getChildren();
    }
  }

  private final class MediaItemTreeCell extends TreeCell<MediaItem> {
    private final CellProvider<MediaItem> provider;

    private MediaItemTreeCell(CellProvider<MediaItem> provider) {
      this.provider = provider;

      setDisclosureNode(new Group());
    }

    @Override
    protected void updateItem(final MediaItem item, boolean empty) {
      super.updateItem(item, empty);

      if(!empty) {
        setGraphic(provider.configureCell(getTreeItem()));
      }
    }
  }
}
