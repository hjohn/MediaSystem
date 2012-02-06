package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.EpisodesMediaTree;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.SelectMediaPane.ItemEvent;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.ImageCache;

import java.util.List;

import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import javax.inject.Inject;

public class SelectMediaPresentation {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final SelectMediaPane<MediaItem> view;
  private final TreeItem<MediaItem> treeRoot = new TreeItem<>();
  private final Navigator navigator;

  private MediaItem currentItem;

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaPane<MediaItem> view) {
    this.navigator = new Navigator(controller.getNavigator());
    this.view = view;

    view.setRoot(treeRoot);

    view.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          navigator.back();
          event.consume();
        }
      }
    });

    view.onItemFocused().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        if(event.getTreeItem() != null) {
          currentItem = event.getTreeItem().getValue();
          bind(event.getTreeItem().getValue());
        }
        event.consume();
      }
    });

    view.onItemSelected().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        final MediaItem mediaItem = event.getTreeItem().getValue();
        System.out.println("[FINE] SelectMediaPresentation.SelectMediaPresentation(...).new EventHandler() {...}.handle() - item selected: " + mediaItem);

        if(mediaItem.isLeaf()) {
          controller.play(mediaItem);
        }
        else if(mediaItem.isRoot()) {
          setMediaTree(mediaItem.getRoot());
        }
        else if(mediaItem instanceof hs.mediasystem.framework.Group) {
          event.getTreeItem().setExpanded(true);
        }
        event.consume();
      }
    });

    view.onItemAlternateSelect().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        final MediaItem mediaItem = event.getTreeItem().getValue();

        List<? extends Option> options = FXCollections.observableArrayList(
          new ActionOption("Reload meta data", new Callable<Boolean>() {
            @Override
            public Boolean call() {
              // enrichItem(mediaItem, true);  // FIXME

              ImageCache.expunge(currentItem.getPoster());
              ImageCache.expunge(currentItem.getBackground());
              // updateCurrentItem();
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
          oldLabel.setText("" + ((MediaItem)oldValue.getUserData()).getSeason());
        }
        label.setText("Season " + ((MediaItem)value.getUserData()).getSeason());

        refilter();
      }
    });
  }

  public SelectMediaPane<MediaItem> getView() {
    return view;
  }

  public void setMediaTree(final MediaTree mediaTree) {
    navigator.navigateTo(new Destination("Movie...") {
      @Override
      public void go() {
        boolean filterLevel = mediaTree instanceof EpisodesMediaTree;

        view.filterItemsProperty().clear();

        if(filterLevel) {
          for(MediaItem item : mediaTree.children()) {
            Label label = new Label("" + item.getSeason());

            view.filterItemsProperty().add(label);
            label.setUserData(item);
          }

          view.activeFilterItemProperty().set(view.filterItemsProperty().get(0));

          refilter();
        }
        else {
          treeRoot.getChildren().clear();

          for(MediaItem item : mediaTree.children()) {
            treeRoot.getChildren().add(new MediaTreeItem(item));
          }
        }

        view.setCellFactory(new Callback<TreeView<MediaItem>, TreeCell<MediaItem>>() {
          @Override
          public TreeCell<MediaItem> call(TreeView<MediaItem> param) {
            return new MediaItemTreeCell(mediaTree.getCellProvider());
          }
        });
      }
    });
  }

  private void refilter() {
    treeRoot.getChildren().clear();
    hs.mediasystem.framework.Group group = (hs.mediasystem.framework.Group)view.activeFilterItemProperty().get().getUserData();

    for(MediaItem item : group.children()) {
      treeRoot.getChildren().add(new MediaTreeItem(item));
    }
  }

  private void bind(final MediaItem mediaItem) {
    if(mediaItem != null) {
      view.titleProperty().bind(mediaItem.titleProperty());
      view.subtitleProperty().bind(mediaItem.subtitleProperty());
      view.releaseTimeProperty().bind(MediaItemFormatter.releaseTimeBinding(mediaItem));
      view.ratingProperty().bind(mediaItem.ratingProperty());
      view.plotProperty().bind(mediaItem.plotProperty());
      view.runtimeProperty().bind(mediaItem.runtimeProperty());
      view.backgroundProperty().bind(mediaItem.backgroundProperty());  // TODO Expensive image loading being done on JavaFX thread
      view.posterProperty().bind(mediaItem.posterProperty());

      view.genresProperty().bind(new StringBinding() {
        {
          bind(mediaItem.genresProperty());
        }

        @Override
        protected String computeValue() {
          String genreText = "";

          for(String genre : mediaItem.genresProperty().get()) {
            if(!genreText.isEmpty()) {
              genreText += " • ";
            }

            genreText += genre;
          }

          return genreText;
        }
      });
    }
  }

  private static final class MediaTreeItem extends TreeItem<MediaItem> {
    private boolean childrenPopulated;

    private MediaTreeItem(MediaItem value) {
      super(value);
    }

    @Override
    public boolean isLeaf() {
      return !(getValue() instanceof hs.mediasystem.framework.Group);
    }

    @Override
    public ObservableList<TreeItem<MediaItem>> getChildren() {
      if(!childrenPopulated) {
        childrenPopulated = true;

        if(getValue() instanceof hs.mediasystem.framework.Group) {
          hs.mediasystem.framework.Group group = (hs.mediasystem.framework.Group)getValue();

          for(MediaItem child : group.children()) {
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
        setGraphic(provider.configureCell(item));
      }
    }
  }
}
