package hs.mediasystem;

import hs.mediasystem.SelectMediaPane.ItemEvent;
import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.CellProvider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
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
  private final TreeItem<MediaItem> treeRoot = new TreeItem<>();

  private MediaItem currentItem;

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaPane<MediaItem> view, ItemEnricher itemEnricher) {
    this.view = view;
    this.itemEnricher = itemEnricher;

    view.setRoot(treeRoot);
    view.onItemFocused().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        currentItem = event.getTreeItem().getValue();
        updateCurrentItem();  // TODO Expensive image loading being done on JavaFX thread
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

  private void updateCurrentItem() {
    if(currentItem != null) {
      view.setTitle(currentItem.getTitle());
      view.setSubtitle(currentItem.getSubtitle());
      view.setReleaseYear(currentItem.getReleaseYear() == null ? "" : "" + currentItem.getReleaseYear());
      view.setPlot(currentItem.getPlot());
      view.setPoster(currentItem.getPoster());
      view.setBackground(currentItem.getBackground());
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
        if(loadTask != null) {

          /*
           * Cancel any load task before updating the graphic, as otherwise an older use of
           * this cell might update it with the wrong data when the task completes.
           */

          loadTask.cancel();
        }

        setGraphic(createNodeGraphic(item));

        if(!item.isEnriched()) {
          loadTask = new Task<Void>() {
            @Override
            public Void call() {
              System.out.println("[FINE] SelectMediaPresentation.MediaItemTreeCell.updateItem(...).new Task() {...}.call() - Loading data for: " + item);

              item.loadData(itemEnricher);

              return null;
            }
          };

          loadTask.stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> source, State oldState, State state) {
              if(state == State.SUCCEEDED) {
                setGraphic(createNodeGraphic(item));
                if(item.equals(currentItem)) {
                  updateCurrentItem();
                }
              }
              else if(state == State.FAILED) {
                System.err.println("Exception while enriching: " + item);
                loadTask.getException().printStackTrace();
              }
            }
          });

          new Thread(loadTask).start();
        }
      }
    }
  }
}
