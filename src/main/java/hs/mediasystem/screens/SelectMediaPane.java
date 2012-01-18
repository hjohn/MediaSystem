package hs.mediasystem.screens;

import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Group;
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
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

public class SelectMediaPane<T> extends StackPane {
  private final TreeView<T> treeView = new TreeView<>();

  private final ObjectProperty<String> title = new SimpleObjectProperty<>();
  private final ObjectProperty<String> subtitle = new SimpleObjectProperty<>();
  private final ObjectProperty<String> releaseYear = new SimpleObjectProperty<>();
  private final ObjectProperty<String> plot = new SimpleObjectProperty<>();

  private final ObjectProperty<Image> poster = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> background = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> newBackground = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> wantedBackground = new SimpleObjectProperty<>();

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

  public SelectMediaPane() {
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

    final GridPane root = new GridPane();

    root.getStyleClass().addAll("select-item-pane", "content-box-grid");

    treeView.setId("selectItem-listView");

    treeView.setEditable(false);
    treeView.setShowRoot(false);

    treeView.getFocusModel().focusedItemProperty().addListener(new ChangeListener<TreeItem<T>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<T>> observable, TreeItem<T> oldValue, final TreeItem<T> newValue) {
        if(onItemFocused.get() != null) {
          onItemFocused.get().handle(new ItemEvent<>(newValue));
        }
      }
    });

    treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && onItemSelected.get() != null) {
          onItemSelected.get().handle(new ItemEvent<>(treeView.getSelectionModel().getSelectedItem()));
        }
      }
    });

    treeView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER && onItemSelected.get() != null) {
          onItemSelected.get().handle(new ItemEvent<>(treeView.getSelectionModel().getSelectedItem()));
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

          // HACK: Following three lines is a dirty hack to get the ImageView to respect the size of its parent
          setManaged(false);
          fitWidthProperty().bind(widthProperty().subtract(hgapProperty()));  // TODO seems to bug when redisplaying this scene after being hidden?
          fitHeightProperty().bind(heightProperty());
        }});
      }}, 0, 0);
      add(new BorderPane() {{
        setTop(new VBox() {{
          getStyleClass().addAll("item-details");
          getChildren().add(new Label("title") {{
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

    getChildren().add(new BorderPane() {{
      setCenter(new Group() {{
        //this.getStyleClass().add("debug-border");
        setMinSize(1, 1);
        getChildren().addAll(backgroundImageView, newBackgroundImageView);
      }});
    }});

    getChildren().add(root);
  }

  public void setRoot(TreeItem<T> root) {
    treeView.setRoot(root);
  }

  public void setCellFactory(Callback<TreeView<T>, TreeCell<T>> cellFactory) {
    treeView.setCellFactory(cellFactory);
  }

  private final ObjectProperty<EventHandler<ItemEvent<T>>> onItemFocused = new SimpleObjectProperty<>();
  public ObjectProperty<EventHandler<ItemEvent<T>>> onItemFocused() { return onItemFocused; }

  private final ObjectProperty<EventHandler<ItemEvent<T>>> onItemSelected = new SimpleObjectProperty<>();
  public ObjectProperty<EventHandler<ItemEvent<T>>> onItemSelected() { return onItemSelected; }

  public static class ItemEvent<T> extends Event {
    private final TreeItem<T> treeItem;

    public ItemEvent(TreeItem<T> treeItem) {
      super(EventType.ROOT);
      this.treeItem = treeItem;
    }

    public TreeItem<T> getTreeItem() {
      return treeItem;
    }
  }

  public void setTitle(String title) {
    this.title.set(title);
  }

  public void setSubtitle(String subtitle) {
    this.subtitle.set(subtitle);
  }

  public void setReleaseYear(String releaseYear) {
    this.releaseYear.set(releaseYear);
  }

  public void setPlot(String plot) {
    this.plot.set(plot);
  }

  public void setPoster(ImageHandle poster) {
    trySetImage(poster, this.poster);
  }

  public void setBackground(ImageHandle background) {
    if(trySetImage(background, wantedBackground, backgroundImageView.getFitWidth(), backgroundImageView.getFitHeight())) {
      if(timeline.getStatus() == Animation.Status.STOPPED) {
        newBackground.set(wantedBackground.get());
        timeline.play();
      }
    }
  }

  private static boolean trySetImage(ImageHandle handle, ObjectProperty<Image> image) {
    if(handle != null) {
      image.set(ImageCache.loadImage(handle));
      return true;
    }

    return false;
  }

  private static boolean trySetImage(ImageHandle handle, ObjectProperty<Image> image, double w, double h) {
    if(handle != null) {
      image.set(ImageCache.loadImage(handle, w, h, true));
      return true;
    }

    return false;
  }
}