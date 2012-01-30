package hs.mediasystem.screens;

import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.EpisodesMediaTree;
import hs.mediasystem.screens.SelectMediaPane.ItemEvent;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.ImageCache;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
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
  private final CachedItemEnricher itemEnricher;
  private final TreeItem<MediaItem> treeRoot = new TreeItem<>();

  private DialogScreen dialogScreen;
  private MediaItem currentItem;

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaPane<MediaItem> view, CachedItemEnricher itemEnricher) {
    this.view = view;
    this.itemEnricher = itemEnricher;

    view.setRoot(treeRoot);
    view.onItemFocused().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        if(event.getTreeItem() != null) {
          currentItem = event.getTreeItem().getValue();
          updateCurrentItem();  // TODO Expensive image loading being done on JavaFX thread
        }
      }
    });

    view.onItemSelected().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        MediaItem mediaItem = event.getTreeItem().getValue();
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
      }
    });

    view.onItemAlternateSelect().set(new EventHandler<ItemEvent<MediaItem>>() {
      @Override
      public void handle(ItemEvent<MediaItem> event) {
        final MediaItem mediaItem = event.getTreeItem().getValue();

        if(!view.getChildren().contains(dialogScreen)) {
          List<? extends Option> options = FXCollections.observableArrayList(
            new ActionOption("Reload meta data", new Callable<Boolean>() {
              @Override
              public Boolean call() {
                enrichItem(mediaItem, true);
                ImageCache.expunge(currentItem.getPoster());
                ImageCache.expunge(currentItem.getBackground());
                updateCurrentItem();
                return true;
              }
            })
          );

          dialogScreen = new DialogScreen("Video - Options", options);

          view.getChildren().add(dialogScreen);

          dialogScreen.requestFocus();

          dialogScreen.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
              if(BACK_SPACE.match(event)) {
                view.getChildren().remove(dialogScreen);
                dialogScreen = null;
                event.consume();
              }
            }
          });
        }
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

  private void refilter() {
    treeRoot.getChildren().clear();
    hs.mediasystem.framework.Group group = (hs.mediasystem.framework.Group)view.activeFilterItemProperty().get().getUserData();

    for(MediaItem item : group.children()) {
      treeRoot.getChildren().add(new MediaTreeItem(item));
    }
  }

  private void updateCurrentItem() {
    if(currentItem != null) {
      view.setTitle(currentItem.getTitle());
      view.setSubtitle(currentItem.getSubtitle());
      view.setReleaseTime(MediaItemFormatter.formatReleaseTime(currentItem));
      view.setPlot(currentItem.getPlot());
      view.setRating(currentItem.getRating() == null ? 0.0 : currentItem.getRating());
      view.setRuntime(currentItem.getRuntime());
      view.setGenres(currentItem.getGenres());
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

  private void enrichItem(final MediaItem mediaItem, boolean bypassCache) {
    try {
      LocalInfo localInfo = mediaItem.getLocalInfo();
      Identifier identifier = itemEnricher.identifyItem(localInfo, bypassCache);

      Item item = itemEnricher.loadItem(identifier, bypassCache);

      mediaItem.setTitle(item.getTitle());
      mediaItem.setBanner(item.getBanner());
      mediaItem.setPoster(item.getPoster());
      mediaItem.setBackground(item.getBackground());
      mediaItem.setPlot(item.getPlot());
      mediaItem.setRating(item.getRating());
      mediaItem.setReleaseDate(item.getReleaseDate());
      mediaItem.setGenres(item.getGenres());
      mediaItem.setLanguage(item.getLanguage());
      mediaItem.setTagline(item.getTagline());
      mediaItem.setRuntime(item.getRuntime());
    }
    catch(IdentifyException e) {
      System.out.println("[WARN] SelectMediaPresentation.enrichItem() - Enrichment failed of " + mediaItem + " failed with exception: " + e);
      e.printStackTrace(System.out);
    }

    mediaItem.setEnriched(true);  // set to true, even if something failed as otherwise we keep trying to enrich
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

      if(!empty) {
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

              synchronized(SelectMediaPresentation.class) {  // TODO so only one gets updated at the time globally...
                enrichItem(item, false);
              }

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
