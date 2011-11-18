package hs.mediasystem;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.screens.movie.ItemUpdate;
import hs.models.events.EventListener;
import hs.ui.image.ImageHandle;

import java.io.ByteArrayInputStream;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class SelectItemScene {
  private final ObjectProperty<Image> poster = new SimpleObjectProperty<Image>();
  private final ObjectProperty<Image> background = new SimpleObjectProperty<Image>();

  public Node show(Stage stage, final MediaTree mediaTree) {
    final GridPane root = new GridPane();
    
    root.getStyleClass().add("content-box-grid");
        
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
          trySetImage(newValue.getPoster(), poster);
          trySetImage(newValue.getBackground(), background);
        }
      });
    }};
    
    mediaTree.onItemUpdate().call(new EventListener<ItemUpdate>() {
      @Override
      public void onEvent(ItemUpdate event) {
        if(event.getItem().equals(listView.getSelectionModel().getSelectedItem())) {
          trySetImage(event.getItem().getPoster(), poster);
          trySetImage(event.getItem().getBackground(), background);
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
        setPercentHeight(100);
      }}
    );
  
    root.add(new HBox() {{
      getStyleClass().add("content-box");
      getChildren().add(new VBox() {{
        HBox.setHgrow(this, Priority.ALWAYS);
        
        final ReadOnlyDoubleProperty width = widthProperty();
        final ReadOnlyDoubleProperty height = heightProperty();
        
        getChildren().add(new ImageView() {{
          imageProperty().bind(poster);
          fitWidthProperty().bind(width);
          fitHeightProperty().bind(height);
          setPreserveRatio(true);
          setSmooth(true);
        }});
      }});
    }}, 0, 0);
    
    root.add(new HBox() {{
      getStyleClass().add("content-box");
      getChildren().add(listView);
      
      HBox.setHgrow(listView, Priority.ALWAYS);
    }}, 1, 0);
        
    return new StackPane() {{
      final ReadOnlyDoubleProperty width = widthProperty();
      final ReadOnlyDoubleProperty height = heightProperty();
      
      getChildren().add(new ScrollPane() {{
        setContent(new ImageView() {{
  //        setClip(new HBox() {{
  //          maxWidthProperty().bind(width);
  //          maxHeightProperty().bind(height);
  //        }});
  //        setViewport(new Rectangle2D(1, 1, 1, 1));
  //        viewportProperty().bind(this.);
  //        maxWidthProperty().bind(width);
  //        maxHeightProperty().bind(height);
          imageProperty().bind(background);
          
  //        fitWidthProperty().bind(width);
  //        fitHeightProperty().bind(height);
          setPreserveRatio(true);
          setSmooth(true);
        }});
      }});
      getChildren().add(root);
    }};
  }
  
  public static void trySetImage(ImageHandle handle, ObjectProperty<Image> image) {
    if(handle != null) {
      image.set(new Image(new ByteArrayInputStream(handle.getImageData())));
    }
  }
}

