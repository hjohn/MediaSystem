package hs.mediasystem.screens;

import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);
  private static final KeyCombination LEFT = new KeyCodeCombination(KeyCode.LEFT);
  private static final KeyCombination RIGHT = new KeyCodeCombination(KeyCode.RIGHT);

  private final TreeView<T> treeView = new TreeView<>();

  private final StringProperty title = new SimpleStringProperty();
  private final StringProperty subtitle = new SimpleStringProperty();
  private final StringProperty releaseTime = new SimpleStringProperty();
  private final StringProperty plot = new SimpleStringProperty();
  private final DoubleProperty rating = new SimpleDoubleProperty();
  private final StringProperty genres = new SimpleStringProperty();
  private final IntegerProperty runtime = new SimpleIntegerProperty();

  private final ObjectProperty<ImageHandle> backgroundHandle = new SimpleObjectProperty<>();
  private final ObjectProperty<ImageHandle> posterHandle = new SimpleObjectProperty<>();

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

  private final BorderPane listPane = new BorderPane() {{
    getStyleClass().addAll("content-box", "list");
  }};

  private final GridPane detailPane = new GridPane() {{
    getStyleClass().addAll("content-box", "detail");

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
        setPercentHeight(100);
      }}
    );
  }};

  private final Filter filter = new Filter() {{
    getStyleClass().add("seasons");
    getChildren().add(new Label("Season 1"));
    getChildren().add(new Label("Season 2"));
  }};

  private final Timeline timeline = new Timeline(
    new KeyFrame(Duration.ZERO,
      new KeyValue(backgroundImageView.opacityProperty(), 1.0),
      new KeyValue(newBackgroundImageView.opacityProperty(), 0.0)
    ),
    new KeyFrame(new Duration(4000),
      new KeyValue(backgroundImageView.opacityProperty(), 0.0),
      new KeyValue(newBackgroundImageView.opacityProperty(), 1.0)
    )
  );

  private final Timeline intro = new Timeline(
    new KeyFrame(Duration.ZERO,
      new KeyValue(listPane.translateXProperty(), 1500),
      new KeyValue(detailPane.translateXProperty(), -1500)
    ),
    new KeyFrame(Duration.seconds(1),
      new KeyValue(listPane.translateXProperty(), 0),
      new KeyValue(detailPane.translateXProperty(), 0)
    )
  );

  public SelectMediaPane() {
    setId("select-media");

    intro.play();

    timeline.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        background.set(newBackground.get());
        backgroundImageView.setOpacity(1.0);
        newBackgroundImageView.setOpacity(0.0);

        if(wantedBackground.get() == null || !wantedBackground.get().equals(background.get())) {
          newBackground.set(wantedBackground.get());
          timeline.play();
        }
      }
    });

    final GridPane root = new GridPane();

    root.getStyleClass().addAll("content-box-grid");

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
        TreeItem<T> focusedItem = treeView.getFocusModel().getFocusedItem();

        if(focusedItem != null) {
          if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && onItemSelected.get() != null) {
            System.out.println("[FINE] SelectMediaPane.SelectMediaPane().new EventHandler() {...}.handle() - double clicked");
            onItemSelected.get().handle(new ItemEvent<>(focusedItem));
          }
        }
      }
    });

    treeView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        TreeItem<T> focusedItem = treeView.getFocusModel().getFocusedItem();

        if(focusedItem != null) {
          if(ENTER.match(event) && onItemSelected.get() != null) {
            onItemSelected.get().handle(new ItemEvent<>(focusedItem));
            event.consume();
          }
          else if(KEY_O.match(event) && onItemAlternateSelect.get() != null) {
            onItemAlternateSelect.get().handle(new ItemEvent<>(focusedItem));
            event.consume();
          }
        }

        if(LEFT.match(event)) {
          filter.activatePrevious();
          event.consume();
        }
        else if(RIGHT.match(event)) {
          filter.activateNext();
          event.consume();
        }
        else if(BACK_SPACE.match(event) && onBack.get() != null) {
          ActionEvent actionEvent = new ActionEvent();
          onBack.get().handle(actionEvent);
          if(actionEvent.isConsumed()) {
            event.consume();
          }
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

    detailPane.add(new ScrollPane() {{
      final ReadOnlyDoubleProperty widthProperty = widthProperty();
      final ReadOnlyDoubleProperty heightProperty = heightProperty();

      setHbarPolicy(ScrollBarPolicy.NEVER);
      setVbarPolicy(ScrollBarPolicy.NEVER);

      setContent(new ImageView() {{
        imageProperty().bind(poster);
        setPreserveRatio(true);
        setSmooth(true);
        setEffect(new DropShadow());
//          setEffect(new PerspectiveTransform() {{
//            setUlx(10.0);
//            setUly(10.0);
//            setUrx(310.0);
//            setUry(40.0);
//            setLrx(310.0);
//            setLry(60.0);
//            setLlx(10.0);
//            setLly(90.0);
//            setEffect(new Reflection() {{
//              setFraction(0.10);
//            }});
//          }});

        fitWidthProperty().bind(widthProperty);
        fitHeightProperty().bind(heightProperty);
      }});
    }}, 0, 0);
    detailPane.add(new BorderPane() {{
      setTop(new VBox() {{
        visibleProperty().bind(title.isNotNull());
        getChildren().add(new Label() {{
          getStyleClass().add("title");
          textProperty().bind(title);
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("subtitle");
          textProperty().bind(subtitle);
          managedProperty().bind(textProperty().isNotEqualTo(""));
        }});
        getChildren().add(new HBox() {{
          setAlignment(Pos.CENTER_LEFT);
          getChildren().add(new StarRating(12, 5, 5) {{
            ratingProperty().bind(rating.divide(10));
          }});
          getChildren().add(new Label() {{
            getStyleClass().add("rating");
            textProperty().bind(Bindings.format("%3.1f/10", rating));
          }});
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("genres");
          textProperty().bind(genres);
          managedProperty().bind(textProperty().isNotEqualTo(""));
        }});
        getChildren().add(new Label("PLOT") {{
          getStyleClass().add("header");
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("plot");
          textProperty().bind(plot);
          VBox.setVgrow(this, Priority.ALWAYS);
        }});
        getChildren().add(new Label("RELEASED") {{
          getStyleClass().add("header");
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("release-time");
          textProperty().bind(releaseTime);
        }});
        getChildren().add(new Label("RUNTIME") {{
          getStyleClass().add("header");
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("runtime");
          textProperty().bind(Bindings.format("%d minutes", runtime));
        }});
      }});
    }}, 1, 0);

    listPane.setTop(filter);
    listPane.setCenter(treeView);

    root.add(detailPane, 0, 1);
    root.add(listPane, 1, 1);

    getChildren().add(new ScrollPane() {{
      final ReadOnlyDoubleProperty widthProperty = widthProperty();
      final ReadOnlyDoubleProperty heightProperty = heightProperty();

      setHbarPolicy(ScrollBarPolicy.NEVER);
      setVbarPolicy(ScrollBarPolicy.NEVER);

      setContent(new Group() {{
        getChildren().addAll(backgroundImageView, newBackgroundImageView);

        backgroundImageView.fitWidthProperty().bind(widthProperty);
        backgroundImageView.fitHeightProperty().bind(heightProperty);
        newBackgroundImageView.fitWidthProperty().bind(widthProperty);
        newBackgroundImageView.fitHeightProperty().bind(heightProperty);
      }});
    }});

    getChildren().add(root);

    backgroundHandle.addListener(new ChangeListener<ImageHandle>() {
      @Override
      public void changed(ObservableValue<? extends ImageHandle> observable, ImageHandle oldValue, ImageHandle newValue) {
        if(trySetImage(newValue, wantedBackground, backgroundImageView.getFitWidth(), backgroundImageView.getFitHeight())) {
          if(timeline.getStatus() == Animation.Status.STOPPED) {
            newBackground.set(wantedBackground.get());
            timeline.play();
          }
        }
      }
    });

    posterHandle.addListener(new ChangeListener<ImageHandle>() {
      @Override
      public void changed(ObservableValue<? extends ImageHandle> observable, ImageHandle oldValue, ImageHandle newValue) {
        trySetImage(newValue, poster);
      }
    });
  }

  public ObservableList<Node> filterItemsProperty() {
    return filter.getChildren();
  }

  public ObjectProperty<Node> activeFilterItemProperty() {
    return filter.activeProperty();
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

  private final ObjectProperty<EventHandler<ItemEvent<T>>> onItemAlternateSelect = new SimpleObjectProperty<>();
  public ObjectProperty<EventHandler<ItemEvent<T>>> onItemAlternateSelect() { return onItemAlternateSelect; }

  private final ObjectProperty<EventHandler<ActionEvent>> onBack = new SimpleObjectProperty<>();
  public ObjectProperty<EventHandler<ActionEvent>> onBack() { return onBack; }

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

  public StringProperty titleProperty() { return title; }
  public StringProperty subtitleProperty() { return subtitle; }
  public StringProperty releaseTimeProperty() { return releaseTime; }
  public StringProperty plotProperty() { return plot; }
  public DoubleProperty ratingProperty() { return rating; }
  public IntegerProperty runtimeProperty() { return runtime; }
  public StringProperty genresProperty() { return genres; }
  public ObjectProperty<ImageHandle> backgroundProperty() { return backgroundHandle; }
  public ObjectProperty<ImageHandle> posterProperty() { return posterHandle; }

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