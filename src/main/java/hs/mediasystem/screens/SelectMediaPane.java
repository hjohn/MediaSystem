package hs.mediasystem.screens;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SelectMediaView;
import hs.mediasystem.util.ImageHandle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;

public class SelectMediaPane extends StackPane implements SelectMediaView {
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);
  private static final KeyCombination KEY_C = new KeyCodeCombination(KeyCode.C);
  private static final KeyCombination LEFT = new KeyCodeCombination(KeyCode.LEFT);
  private static final KeyCombination RIGHT = new KeyCodeCombination(KeyCode.RIGHT);

  private final TreeView<MediaNode> treeView = new TreeView<>();

  private final ObjectBinding<MediaItem> mediaItem = new ObjectBinding<MediaItem>() {
    {
      bind(treeView.getFocusModel().focusedItemProperty());
    }

    @Override
    protected MediaItem computeValue() {
      TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

      return focusedItem != null ? focusedItem.getValue().getMediaItem() : null;
    }
  };

  private final StringBinding groupName = Bindings.selectString(mediaItem, "groupName");
  private final StringBinding title = Bindings.selectString(mediaItem, "title");
  private final StringBinding subtitle = Bindings.selectString(mediaItem, "subtitle");
  private final StringBinding releaseTime = MediaItemFormatter.releaseTimeBinding(mediaItem);
  private final StringBinding plot = Bindings.selectString(mediaItem, "plot");
  private final DoubleBinding rating = Bindings.selectDouble(mediaItem, "rating");
  private final IntegerBinding runtime = Bindings.selectInteger(mediaItem, "runtime");
  private final StringBinding genres = new StringBinding() {
    final ObjectBinding<String[]> selectGenres = Bindings.select(mediaItem, "genres");

    {
      bind(selectGenres);
    }

    @Override
    protected String computeValue() {
      String genreText = "";
      String[] genres = selectGenres.get();

      if(genres != null) {
        for(String genre : genres) {
          if(!genreText.isEmpty()) {
            genreText += " • ";
          }

          genreText += genre;
        }
      }

      return genreText;
    }
  };

  private final ObjectBinding<ImageHandle> backgroundHandle = Bindings.select(mediaItem, "background");
  private final ObjectBinding<ImageHandle> posterHandle = Bindings.select(mediaItem, "poster");

  private final ObjectProperty<Image> poster = new AsyncImageProperty(posterHandle);
  private final ObjectProperty<Image> wantedBackground = new AsyncImageProperty(backgroundHandle);

  private final ObjectProperty<Image> background = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> newBackground = new SimpleObjectProperty<>();

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

    treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

        if(focusedItem != null) {
          if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            itemSelected(event, focusedItem);
          }
          else if(event.getButton() == MouseButton.SECONDARY && event.getClickCount() == 1) {
            dispatchEvent(onItemAlternateSelect, new MediaNodeEvent(focusedItem.getValue()), event);
          }
        }
      }
    });

    treeView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

        if(focusedItem != null) {
          if(ENTER.match(event)) {
            itemSelected(event, focusedItem);
          }
          else if(KEY_C.match(event)) {
            dispatchEvent(onItemAlternateSelect, new MediaNodeEvent(focusedItem.getValue()), event);
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
      }
    });

    addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          dispatchEvent(onBack, new ActionEvent(SelectMediaPane.this, null), event);
        }
      }
    });

    filter.activeProperty().addListener(new ChangeListener<Node>() {
      @Override
      public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node value) {
        Label oldLabel = (Label)oldValue;
        Label label = (Label)value;

        if(oldLabel != null) {
          oldLabel.setText(((MediaNode)oldValue.getUserData()).getMediaItem().getShortTitle());
        }
        label.setText(((MediaNode)value.getUserData()).getMediaItem().getTitle());

        refilter();
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

      setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

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
          getStyleClass().add("group-name");
          textProperty().bind(groupName);
          managedProperty().bind(groupName.isNotEqualTo(""));
          visibleProperty().bind(groupName.isNotEqualTo(""));
        }});
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
          managedProperty().bind(rating.greaterThan(0.0));
          visibleProperty().bind(rating.greaterThan(0.0));
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("genres");
          textProperty().bind(genres);
          managedProperty().bind(textProperty().isNotEqualTo(""));
        }});
        getChildren().add(new Label("PLOT") {{
          getStyleClass().add("header");
          managedProperty().bind(plot.isNotEqualTo(""));
          visibleProperty().bind(plot.isNotEqualTo(""));
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("plot");
          textProperty().bind(plot);
          managedProperty().bind(plot.isNotEqualTo(""));
          visibleProperty().bind(plot.isNotEqualTo(""));
          VBox.setVgrow(this, Priority.ALWAYS);
        }});
        getChildren().add(new FlowPane() {{
          getStyleClass().add("fields");
          getChildren().add(new VBox() {{
            getChildren().add(new Label("RELEASED") {{
              getStyleClass().add("header");
            }});
            getChildren().add(new Label() {{
              getStyleClass().add("release-time");
              textProperty().bind(releaseTime);
            }});
            managedProperty().bind(releaseTime.isNotEqualTo(""));
            visibleProperty().bind(releaseTime.isNotEqualTo(""));
          }});
          getChildren().add(new VBox() {{
            getChildren().add(new Label("RUNTIME") {{
              getStyleClass().add("header");
            }});
            getChildren().add(new Label() {{
              getStyleClass().add("runtime");
              textProperty().bind(Bindings.format("%d minutes", runtime));
            }});
            managedProperty().bind(runtime.greaterThan(0.0));
            visibleProperty().bind(runtime.greaterThan(0.0));
          }});
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

      setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

      setContent(new Group() {{
        getChildren().addAll(backgroundImageView, newBackgroundImageView);

        backgroundImageView.fitWidthProperty().bind(widthProperty);
        backgroundImageView.fitHeightProperty().bind(heightProperty);
        newBackgroundImageView.fitWidthProperty().bind(widthProperty);
        newBackgroundImageView.fitHeightProperty().bind(heightProperty);
      }});
    }});

    getChildren().add(root);

    wantedBackground.addListener(new ChangeListener<Image>() {
      @Override
      public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue) {
        if(timeline.getStatus() == Animation.Status.STOPPED) {
          newBackground.set(wantedBackground.get());
          timeline.play();
        }
      }
    });
  }

  private <E extends Event> void dispatchEvent(ObjectProperty<EventHandler<E>> eventHandlerProperty, E event, Event originatingEvent) {
    EventHandler<E> eventHandler = eventHandlerProperty.get();

    if(eventHandler != null) {
      eventHandler.handle(event);
      if(event.isConsumed() && originatingEvent != null) {
        originatingEvent.consume();
      }
    }
  }

  private void itemSelected(Event event, TreeItem<MediaNode> focusedItem) {
    if(focusedItem.isLeaf()) {
      dispatchEvent(onItemSelected, new MediaNodeEvent(focusedItem.getValue()), event);
    }
    else {
      focusedItem.setExpanded(!focusedItem.isExpanded());
      event.consume();
    }
  }

  @Override
  public void requestFocus() {
    super.requestFocus();
    treeView.requestFocus();
    treeView.getFocusModel().focus(0);
  }

  @Override
  public void setRoot(final MediaNode root) {
    TreeItem<MediaNode> treeRoot = new TreeItem<>(root);

    setCellFactory(new Callback<TreeView<MediaNode>, TreeCell<MediaNode>>() {
      @Override
      public TreeCell<MediaNode> call(TreeView<MediaNode> param) {
        return new MediaItemTreeCell(root.getCellProvider());
      }
    });

    treeView.setRoot(treeRoot);

    filter.getChildren().clear();

    boolean expandTopLevel = root.expandTopLevel();

    if(expandTopLevel) {
      for(MediaNode node : root.getChildren()) {
        Label label = new Label(node.getMediaItem().getShortTitle());

        filter.getChildren().add(label);
        label.setUserData(node);
      }

      filter.activeProperty().set(filter.getChildren().get(0));
    }
    else {
      treeRoot.getChildren().clear();

      for(MediaNode node : root.getChildren()) {
        treeRoot.getChildren().add(new MediaNodeTreeItem(node));
      }
    }
  }

  private void refilter() {
    TreeItem<MediaNode> treeRoot = treeView.getRoot();

    treeRoot.getChildren().clear();

    MediaNode group = (MediaNode)filter.activeProperty().get().getUserData();

    for(MediaNode item : group.getChildren()) {
      treeRoot.getChildren().add(new MediaNodeTreeItem(item));
    }
  }

  public void setCellFactory(Callback<TreeView<MediaNode>, TreeCell<MediaNode>> cellFactory) {
    treeView.setCellFactory(cellFactory);
  }

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onItemSelected = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onItemSelected() { return onItemSelected; }

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onItemAlternateSelect = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onItemAlternateSelect() { return onItemAlternateSelect; }

  private final ObjectProperty<EventHandler<ActionEvent>> onBack = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<ActionEvent>> onBack() { return onBack; }

  private final class MediaNodeTreeItem extends TreeItem<MediaNode> {
    private boolean childrenPopulated;

    private MediaNodeTreeItem(MediaNode value) {
      super(value);
    }

    @Override
    public boolean isLeaf() {
      return getValue().isLeaf();
    }

    @Override
    public ObservableList<TreeItem<MediaNode>> getChildren() {
      if(!childrenPopulated) {
        childrenPopulated = true;

        if(getValue().hasChildren()) {
          for(MediaNode child : getValue().getChildren()) {
            super.getChildren().add(new MediaNodeTreeItem(child));
          }
        }
      }

      return super.getChildren();
    }
  }

  private final class MediaItemTreeCell extends TreeCell<MediaNode> {
    private final CellProvider<MediaNode> provider;

    private MediaItemTreeCell(CellProvider<MediaNode> provider) {
      this.provider = provider;

      setDisclosureNode(new Group());
    }

    @Override
    protected void updateItem(final MediaNode item, boolean empty) {
      super.updateItem(item, empty);

      if(!empty) {
        setGraphic(provider.configureCell(getTreeItem()));
      }
    }
  }
}