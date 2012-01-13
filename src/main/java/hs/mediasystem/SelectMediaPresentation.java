package hs.mediasystem;

import hs.mediasystem.SelectMediaPane.ItemEvent;
import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItem.State;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.CellProvider;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

import javax.inject.Inject;

public class SelectMediaPresentation {
  private final SelectMediaPane<MediaItem> view;
  private final ItemEnricher itemEnricher;
  private final MediaItemUpdateService mediaItemUpdateService = new MediaItemUpdateService();

  private TreeItem<MediaItem> treeRoot = new TreeItem<>();

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaPane<MediaItem> view, ItemEnricher itemEnricher) {
    this.view = view;
    this.itemEnricher = itemEnricher;

    view.setRoot(treeRoot);
    view.onItemFocused().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        if(event.getTreeItem() != null) {
          mediaItemUpdateService.setMediaItem(event.getTreeItem().getValue());
          mediaItemUpdateService.restart();
        }
      }
    });
    view.onItemSelected().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        MediaItem mediaItem = event.getTreeItem().getValue();

        if(mediaItem.isLeaf()) {
          controller.play(mediaItem);
        }
        else if(mediaItem.isRoot()) {
          setMediaTree(mediaItem.getRoot());
        }
        else if(mediaItem instanceof hs.mediasystem.framework.Group) {
          event.getTreeItem().setExpanded(true);
        }
      }
    });
  }

  public SelectMediaPane<MediaItem> getView() {
    return view;
  }

  public void setMediaTree(final MediaTree mediaTree) {
    treeRoot.getChildren().clear();

    for(MediaItem item : mediaTree.children()) {
      treeRoot.getChildren().add(new MediaTreeItem(item));
    }

    view.setCellFactory(new Callback<TreeView<MediaItem>, TreeCell<MediaItem>>() {
      @Override
      public TreeCell<MediaItem> call(TreeView<MediaItem> param) {
        return new MediaItemTreeCell(mediaTree.createListCell());
      }
    });
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

    private Task<Void> loadTask;

    private MediaItemTreeCell(CellProvider<MediaItem> provider) {
      this.provider = provider;

      setDisclosureNode(new Group());
    }

    private Node createNodeGraphic(MediaItem item) {
      return provider.configureCell(item);
    }

    @Override
    protected void updateItem(final MediaItem item, boolean empty) {
      super.updateItem(item, empty);

      if(item != null) {
        setGraphic(createNodeGraphic(item));

        if(item.stateProperty().get() != State.ENRICHED) {
          if(loadTask != null) {
            loadTask.cancel();
          }

          loadTask = new Task<Void>() {  // TODO service?
            @Override
            public Void call() {
              System.out.println("[FINE] SelectMediaPresentation.MediaItemTreeCell.updateItem(...).new Task() {...}.call() - Loading data for: " + item);

              try {
                item.loadData(itemEnricher);
              }
              catch(Exception e) {
                e.printStackTrace();
              }

              return null;
            }
          };

          item.stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> source, State oldState, State newState) {
              if(newState.equals(State.ENRICHED)) {
                Platform.runLater(new Runnable() {
                  @Override
                  public void run() {
                    setGraphic(createNodeGraphic(item));
                  }
                });
              }
            }
          });

          new Thread(loadTask).start();
        }
      }
    }
  }

  private class MediaItemUpdateService extends Service<Void> {  // TODO also needs to be used when MediaItem changed to ENRICHED state
    private MediaItem mediaItem;

    public void setMediaItem(MediaItem value) {
      mediaItem = value;
    }

    @Override
    protected Task<Void> createTask() {
      final MediaItem item = mediaItem;

      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              view.setTitle(item.getTitle());
              view.setSubtitle(item.getSubtitle());
              view.setReleaseYear(item.getReleaseYear() == null ? "" : "" + item.getReleaseYear());
              view.setPlot(item.getPlot());
              view.setPoster(item.getPoster());
              view.setBackground(item.getBackground());
            }
          });

          return null;
        }
      };
    }
  }
}
