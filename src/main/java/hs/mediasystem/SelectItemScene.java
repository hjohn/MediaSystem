package hs.mediasystem;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.CellProvider;
import hs.mediasystem.screens.movie.ItemUpdate;
import hs.models.events.EventListener;
import hs.ui.image.ImageHandle;
import javafx.concurrent.Worker;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

public class SelectItemScene {
  private final ProgramController controller;
  
  private final ObjectProperty<String> title = new SimpleObjectProperty<String>();
  private final ObjectProperty<String> subtitle = new SimpleObjectProperty<String>();
  private final ObjectProperty<String> releaseYear = new SimpleObjectProperty<String>();
  private final ObjectProperty<String> plot = new SimpleObjectProperty<String>();

  private final ObjectProperty<Image> poster = new SimpleObjectProperty<Image>();
  private final ObjectProperty<Image> background = new SimpleObjectProperty<Image>();
  private final ObjectProperty<Image> newBackground = new SimpleObjectProperty<Image>();
  private final ObjectProperty<Image> wantedBackground = new SimpleObjectProperty<Image>();
  
  private final ImageView backgroundImageView = new ImageView() {{
    imageProperty().bind(background);
    setPreserveRatio(true);
    setSmooth(true);
  }};
  
  private final ImageView newBackgroundImageView = new ImageView() {{
    imageProperty().bind(newBackground);
    setPreserveRatio(true);
    setSmooth(true);
  }};
  
  private final Timeline timeline = new Timeline(
    new KeyFrame(Duration.ZERO, 
      new KeyValue(newBackgroundImageView.opacityProperty(), 0.0),
      new KeyValue(backgroundImageView.opacityProperty(), 1.0)
    ),
    new KeyFrame(new Duration(4000), 
      new KeyValue(newBackgroundImageView.opacityProperty(), 1.0),
      new KeyValue(backgroundImageView.opacityProperty(), 0.0)
    )
  );
    
  public SelectItemScene(ProgramController controller) {
    this.controller = controller;
    timeline.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        background.set(newBackground.get());
        backgroundImageView.setOpacity(1.0);
        newBackgroundImageView.setOpacity(0.0);
        
        if(!wantedBackground.get().equals(background.get())) {
          newBackground.set(wantedBackground.get());
          timeline.play();
        }
      }
    });
  }
  
  private ObservableList<MediaItem> mediaItems = FXCollections.observableArrayList(); 
  
  private TreeItem<MediaItem> treeRoot = new TreeItem<MediaItem>();
  
  public Node create(final MediaTree mediaTree) {
    mediaItems.addAll(mediaTree.children());

    for(MediaItem item : mediaTree.children()) {
      treeRoot.getChildren().add(new TreeItem<MediaItem>(item));
    }

    final GridPane root = new GridPane();
            
    root.getStyleClass().addAll("select-item-pane", "content-box-grid");
    
    final TreeView<MediaItem> treeView = new TreeView<MediaItem>(treeRoot) {{
      setId("selectItem-listView");

      setEditable(false);
      setShowRoot(false);
      setCellFactory(new Callback<TreeView<MediaItem>, TreeCell<MediaItem>>() {
        @Override
        public TreeCell<MediaItem> call(TreeView<MediaItem> param) {
          return new MediaItemTreeCell(mediaTree.createListCell());
        }
      });
      
      getFocusModel().focusedItemProperty().addListener(new ChangeListener<TreeItem<MediaItem>>() {
        @Override
        public void changed(ObservableValue<? extends TreeItem<MediaItem>> observable, TreeItem<MediaItem> oldValue, final TreeItem<MediaItem> newValue) {
          System.err.println("changed called on Thread: " + Thread.currentThread().getName());
//          update(newValue.getValue());
          
          Platform.runLater(new Runnable() {
            public void run() {
              update(newValue.getValue());
            }
          });
        }
      });
      
      addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            System.out.println("Selected " + getSelectionModel().getSelectedItem().getValue().getTitle());
            itemSelected(getSelectionModel().getSelectedItem().getValue());
          }
        }
      });
      
      addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
          if(event.getCode() == KeyCode.ENTER) {
            System.out.println("Selected (with key) " + getSelectionModel().getSelectedItem().getValue().getTitle());
            itemSelected(getSelectionModel().getSelectedItem().getValue());
          }
        }
      });
    }};

    mediaTree.onItemUpdate().call(new EventListener<ItemUpdate>() {
      @Override
      public void onEvent(final ItemUpdate event) {
//        TreeItem<MediaItem> foundItem = findMediaItem(treeRoot, event.getItem());
//        
//        if(foundItem != null) {
//          int index = foundItem.getParent().getChildren().indexOf(foundItem);
//          
//          TreeItem<MediaItem> focusedItem = treeView.getFocusModel().getFocusedItem();
//          int focusedIndex = treeView.getFocusModel().getFocusedIndex();
//          TreeItem<MediaItem> oldItem = foundItem.getParent().getChildren().set(index, new TreeItem<MediaItem>(event.getItem()));
//          
//          if(focusedItem.equals(oldItem)) {
//            treeView.getFocusModel().focus(focusedIndex);  // HACK: Because of the call to "set" above, the focused index can change as the item is replaced.  This code restores it.
//          }
//        }
        
        TreeItem<MediaItem> selectedItem = treeView.getSelectionModel().getSelectedItem();

        if(selectedItem != null && event.getItem().equals(selectedItem.getValue())) {
          Platform.runLater(new Runnable() {
            public void run() {
              update(event.getItem());
            }
          });
        }
      }
      
//      private TreeItem<MediaItem> findMediaItem(TreeItem<MediaItem> treeRoot, MediaItem mediaItem) {
//        for(TreeItem<MediaItem> treeItem : treeRoot.getChildren()) {
//          if(treeItem.getValue().equals(mediaItem)) {
//            return treeItem;
//          }
//          
//          if(!treeItem.isLeaf()) {
//            TreeItem<MediaItem> foundItem = findMediaItem(treeItem, mediaItem);
//            
//            if(foundItem != null) {
//              return foundItem;
//            }
//          }
//        }
//        
//        return null;
//      }
    });

    root.getColumnConstraints().addAll(
      new ColumnConstraints() {{
        setPercentWidth(50);
      }},
      new ColumnConstraints() {{
        setPercentWidth(50);
      }}
    );
    
    root.getRowConstraints().addAll(
      new RowConstraints() {{
        setPercentHeight(25);
      }},
      new RowConstraints() {{
        setPercentHeight(75);
      }}
    );
  
    root.add(new GridPane() {{
      setHgap(10);
      
      getColumnConstraints().addAll(
        new ColumnConstraints() {{
          setPercentWidth(50);
        }},
        new ColumnConstraints() {{
          setPercentWidth(50);
        }}
      );
      
      getRowConstraints().addAll(
        new RowConstraints() {{
          // setVgrow(Priority.NEVER);
          setPercentHeight(100);
        }}
      );
      
      getStyleClass().add("content-box");
      add(new BorderPane() {{
        setTop(new ImageView() {{
          imageProperty().bind(poster);
          setPreserveRatio(true);
          setSmooth(true);
          setEffect(new DropShadow());
          //setEffect(new Reflection());
          
          // HACK: Following three lines is a dirty hack to get the ImageView to respect the size of its parent
          setManaged(false);
          fitWidthProperty().bind(widthProperty().subtract(hgapProperty()));  // TODO seems to bug when redisplaying this scene after being hidden?
          fitHeightProperty().bind(heightProperty());
        }});
      }}, 0, 0);
      add(new BorderPane() {{
        setTop(new VBox() {{
          getStyleClass().addAll("item-details");
          getChildren().add(new Label() {{
            textProperty().bind(title);
            getStyleClass().addAll("movie-name", "title");
          }});
          getChildren().add(new Label() {{
            textProperty().bind(subtitle);
            getStyleClass().addAll("movie-subtitle", "subtitle");
          }});
          getChildren().add(new Label() {{
            textProperty().bind(releaseYear);
            getStyleClass().addAll("year", "subtitle");
          }});
          getChildren().add(new Label("Plot text") {{
            textProperty().bind(plot);
            getStyleClass().addAll("plot");
            setWrapText(true);
            VBox.setVgrow(this, Priority.ALWAYS);
          }});
        }});
      }}, 1, 0);
    }}, 0, 1);
    
    root.add(new HBox() {{
      getStyleClass().add("content-box");
      getChildren().add(treeView);
      
      HBox.setHgrow(treeView, Priority.ALWAYS);
    }}, 1, 1);
        
    return new StackPane() {{
      getChildren().add(new BorderPane() {{
        setCenter(new Group() {{
          getChildren().addAll(backgroundImageView, newBackgroundImageView);
        }});
      }});
      getChildren().add(root);
    }};
  }
  
  private final class MediaItemTreeCell extends TreeCell<MediaItem> {
    private final CellProvider<MediaItem> provider;
    private Task<Void> loadTask;
    
    private MediaItemTreeCell(CellProvider<MediaItem> provider) {
      this.provider = provider;
    }

    @Override
    protected void updateItem(final MediaItem item, boolean empty) {
      super.updateItem(item, empty);
      
      if(item != null) {
        setGraphic(provider.configureCell(item));
        
        if(!item.isDataLoaded()) {  // TODO apparently, updateItem is also called for an invisible cell... this causes the time it is really displayed to assume data is loaded already while we really should wait for it
          if(loadTask != null) {
            loadTask.cancel();
          }
          
          loadTask = new Task<Void>() {  // TODO service?
            public Void call() {
              System.err.println("Loading data for : " + item.getTitle());
              item.loadData();
              return null;
            }
          };
          
          loadTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
            public void changed(ObservableValue<? extends Worker.State> source, Worker.State oldState, Worker.State newState) {
              if(newState.equals(Worker.State.SUCCEEDED)) {
                System.err.println("Setting new graphic for : " + item.getTitle());
                setGraphic(provider.configureCell(item));
              }
            }
          });
          
          new Thread(loadTask).start();
        }
      }
    }
  }


  private class MediaItemUpdateService extends Service<Void> {
    private final ObjectProperty<MediaItem> mediaItem = new SimpleObjectProperty<MediaItem>();
    
    public void setMediaItem(MediaItem value) {mediaItem.set(value);}
    
    @Override
    protected Task<Void> createTask() {
      final MediaItem item = mediaItem.get();
      
      return new Task<Void>() {
        @Override
        protected Void call() throws Exception {
          title.set(item.getTitle());
          subtitle.set(item.getSubtitle());
          releaseYear.set(item.getReleaseYear());
          plot.set(item.getPlot());
          trySetImage(item.getPoster(), poster);
          
          if(trySetImage(item.getBackground(), wantedBackground, backgroundImageView.getFitWidth(), backgroundImageView.getFitHeight())) {
            if(timeline.getStatus() == Animation.Status.STOPPED) {
              newBackground.set(wantedBackground.get());
              timeline.play();
            }
          }
          
          return null;
        };
      };
    }
  };
  
  private MediaItemUpdateService mediaItemUpdateService = new MediaItemUpdateService();
  
  protected void update(final MediaItem item) {
    mediaItemUpdateService.setMediaItem(item);
    mediaItemUpdateService.restart();
    
    
//    Task<Void> task = new Task<Void>() {
//      @Override
//      protected Void call() throws Exception {
//        title.set(item.getTitle());
//        subtitle.set(item.getSubtitle());
//        releaseYear.set(item.getReleaseYear());
//        plot.set(item.getPlot());
//        trySetImage(item.getPoster(), poster);
//        
//        if(trySetImage(item.getBackground(), wantedBackground, backgroundImageView.getFitWidth(), backgroundImageView.getFitHeight())) {
//          if(timeline.getStatus() == Animation.Status.STOPPED) {
//            newBackground.set(wantedBackground.get());
//            timeline.play();
//          }
//        }
//        
//        return null;
//      }
//    };
//    
//    new Thread(task).run();
    

  }
  
  public static boolean trySetImage(ImageHandle handle, ObjectProperty<Image> image) {
    if(handle != null) {
      image.set(ImageCache.loadImage(handle));
      return true;
    }
    
    return false;
  }

  public static boolean trySetImage(ImageHandle handle, ObjectProperty<Image> image, double w, double h) {
    if(handle != null) {
      image.set(ImageCache.loadImage(handle, w, h, true));
      return true;
    }
    
    return false;
  }
  
  @SuppressWarnings("unused")
  private static void debugScene(Node node) {
    if(node == null) {
      return;
    }
    
    if(node instanceof Control) {
      Control c = (Control)node;
      System.out.println(">>> " + c.getClass() + " size = " + c.getWidth() + " x " + c.getHeight() + " upto " + c.getMaxWidth() + " x " + c.getMaxHeight() + " pref " + c.getPrefWidth() + " x " + c.getPrefHeight() + " min " + c.getMinWidth() + " x " + c.getMinHeight());
    }
    else if(node instanceof Region) {
      Region c = (Region)node;
      System.out.println(">>> " + c.getClass() + " size = " + c.getWidth() + " x " + c.getHeight() + " upto " + c.getMaxWidth() + " x " + c.getMaxHeight() + " pref " + c.getPrefWidth() + " x " + c.getPrefHeight() + " min " + c.getMinWidth() + " x " + c.getMinHeight());
    }
    else {
      System.out.println(">>> " + node.getClass());
    }
    
    debugScene(node.getParent());
  }
  

  private void itemSelected(MediaItem selectedItem) {
    if(selectedItem.isLeaf()) {
      controller.play(selectedItem);
    }
  }
}

