package hs.mediasystem;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.screens.movie.ItemUpdate;
import hs.models.events.EventListener;
import hs.ui.image.ImageHandle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class SelectItemScene {
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
  
  public SelectItemScene() {
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
    
  public Node show(final Stage stage, final MediaTree mediaTree) {
    final GridPane root = new GridPane();
            
    root.getStyleClass().addAll("select-item-pane", "content-box-grid");
    
    final ListView<MediaItem> listView = new ListView<MediaItem>() {{
      setId("selectItem-listView");
      getItems().addAll(mediaTree.children());
      
      setCellFactory(new Callback<ListView<MediaItem>, ListCell<MediaItem>>() {
        @Override
        public ListCell<MediaItem> call(ListView<MediaItem> param) {
          return mediaTree.createListCell();
        }
      });
      
      getFocusModel().focusedItemProperty().addListener(new ChangeListener<MediaItem>() {
        @Override
        public void changed(ObservableValue<? extends MediaItem> observable, MediaItem oldValue, MediaItem newValue) {
          System.out.println("Selection changed to mediaItem: " + newValue.getTitle());
          
          update(newValue);
        }
      });
    }};
    
    mediaTree.onItemUpdate().call(new EventListener<ItemUpdate>() {
      @Override
      public void onEvent(final ItemUpdate event) {
        if(event.getItem().equals(listView.getSelectionModel().getSelectedItem())) {
          Platform.runLater(new Runnable() {
            public void run() {
              update(event.getItem());
            }
          });
        }
      }
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
          
          // Following three lines is a dirty hack to get the ImageView to respect the size of its parent
          setManaged(false);
          fitWidthProperty().bind(widthProperty().subtract(hgapProperty()));
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
      getChildren().add(listView);
      
      HBox.setHgrow(listView, Priority.ALWAYS);
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
}

