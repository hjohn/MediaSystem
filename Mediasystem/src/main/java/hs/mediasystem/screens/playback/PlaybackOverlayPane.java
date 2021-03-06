package hs.mediasystem.screens.playback;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.util.GridPaneUtil;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;
import hs.mediasystem.util.SpecialEffects;
import hs.mediasystem.util.StringBinding;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Blend;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PlaybackOverlayPane extends StackPane {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  public final ObjectProperty<Media> media = new SimpleObjectProperty<>();
  public final ObjectProperty<Player> player = new SimpleObjectProperty<>();
  public final BooleanProperty overlayVisible = new SimpleBooleanProperty(true);

  private final PlayerBindings playerBindings = new PlayerBindings(player);

  private final GridPane detailsOverlay = GridPaneUtil.create(new double[] {5, 20, 5, 65, 5}, new double[] {45, 50, 5});

  private final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(media, "image");
  private final AsyncImageProperty poster = new AsyncImageProperty();

  private final PlaybackInfoBorders borders = new PlaybackInfoBorders(playerBindings);

  private final VBox playbackStateOverlay = new VBox() {{
    getStyleClass().add("content-box");
    setVisible(false);
  }};

  private final HBox conditionalStateOverlay = new HBox() {{
    setSpacing(30);
    setAlignment(Pos.BOTTOM_LEFT);
    setFillHeight(false);
  }};

  private final Timeline fadeInSustainAndFadeOut = new Timeline(
    new KeyFrame(Duration.seconds(0)),
    new KeyFrame(Duration.seconds(1), new KeyValue(detailsOverlay.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(6), new KeyValue(detailsOverlay.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(9), new KeyValue(detailsOverlay.opacityProperty(), 0.0))
  );

  public PlaybackOverlayPane() {
    getStylesheets().add("playback-overlay.css");
    getStylesheets().add("playback-state-overlay.css");

    setId("playback-overlay");

    poster.imageHandleProperty().bind(posterHandle);

    playbackStateOverlay.getChildren().addListener(new ListChangeListener<Node>() {
      @Override
      public void onChanged(ListChangeListener.Change<? extends Node> change) {
        playbackStateOverlay.setVisible(!change.getList().isEmpty());
      }
    });

    overlayVisible.addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean old, Boolean current) {
        borders.setLabelVisible(current.booleanValue());
      }
    });

    addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          event.consume();
        }
      }
    });

    setFocusTraversable(true);

    borders.mediaProperty().bind(media);

    detailsOverlay.setId("video-overlay");
    detailsOverlay.add(new ScaledImageView() {{
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setAlignment(Pos.BOTTOM_RIGHT);
      getStyleClass().add("poster");
      setEffect(new Blend() {{
        setBottomInput(new DropShadow());
        setTopInput(new Reflection() {{
          this.setFraction(0.10);
        }});
      }});
    }}, 1, 1);
    detailsOverlay.add(new BorderPane() {{
      setId("video-overlay_info");
      setBottom(new HBox() {{
        getChildren().add(new VBox() {{
          final StringBinding serieName = MapBindings.get(media).thenOrDefault("serie", null).then("title").asStringBinding();
          final StringBinding title = MapBindings.get(media).then("title").asStringBinding();
          final StringBinding subtitle = MapBindings.get(media).then("subtitle").asStringBinding();

          HBox.setHgrow(this, Priority.ALWAYS);
          getChildren().add(new Label() {{
            textProperty().bind(Bindings.when(serieName.isNotNull()).then(serieName).otherwise(title));
            getStyleClass().add("video-title");
            setEffect(SpecialEffects.createNeonEffect(64));
          }});
          getChildren().add(new Label() {{
            textProperty().bind(Bindings.when(serieName.isNotNull()).then(title).otherwise(subtitle));
            getStyleClass().add("video-subtitle");
          }});
        }});
      }});
    }}, 3, 1);

    getChildren().add(detailsOverlay);
    getChildren().add(borders);

    getChildren().add(new GridPane() {{
      setHgap(0);
      getColumnConstraints().add(new ColumnConstraints() {{
        setPercentWidth(5);
      }});
      getColumnConstraints().add(new ColumnConstraints() {{
        setPercentWidth(20);
      }});
      getColumnConstraints().add(new ColumnConstraints() {{
        setPercentWidth(50);
      }});
      getColumnConstraints().add(new ColumnConstraints() {{
        setPercentWidth(25);
      }});
      getRowConstraints().add(new RowConstraints() {{
        setPercentHeight(10);
      }});
      getRowConstraints().add(new RowConstraints());
      getRowConstraints().add(new RowConstraints() {{
        setVgrow(Priority.ALWAYS);
        setFillHeight(true);
      }});
      getRowConstraints().add(new RowConstraints() {{
        setPercentHeight(5);
      }});

      add(playbackStateOverlay, 2, 1);
      add(conditionalStateOverlay, 1, 2);
    }});

    sceneProperty().addListener(o -> {
      if(getScene() == null) {
        fadeInSustainAndFadeOut.stop();
      }
      else {
        fadeInSustainAndFadeOut.playFromStart();
      }
    });

    registerConditionalOSD(playerBindings.muted, new BorderPane() {{
      getStyleClass().add("content-box");
      setCenter(new Region() {{
        setMinSize(40, 40);
        getStyleClass().add("mute-shape");
      }});
    }});

    registerConditionalOSD(playerBindings.paused, new VBox() {{
      getStyleClass().add("content-box");
      getChildren().add(new Region() {{
        setMinSize(40, 40);
        getStyleClass().add("pause-shape");
      }});
    }});
  }

  public void showOSD() {
    fadeInSustainAndFadeOut.playFromStart();
  }

  public final void registerConditionalOSD(BooleanExpression showCondition, final Node node) {  // id of node is used to distinguish same items
    node.setOpacity(0);
    conditionalStateOverlay.getChildren().add(node);

    final Timeline fadeIn = new Timeline(
      new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 0.0)),
      new KeyFrame(Duration.seconds(0.5), new KeyValue(node.opacityProperty(), 1.0))
    );

    final Timeline fadeOut = new Timeline(
      new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 1.0)),
      new KeyFrame(Duration.seconds(0.5), new KeyValue(node.opacityProperty(), 0.0))
    );

    showCondition.addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean value) {
        if(value) {
          fadeIn.play();
        }
        else {
          fadeOut.play();
        }
      }
    });
  }

  public void addOSD(final Node node) {  // id of node is used to distinguish same items
    String id = node.getId();

    for(Node child : playbackStateOverlay.getChildren()) {
      if(id.equals(child.getId())) {
        Timeline timeline = (Timeline)child.getUserData();

        if(timeline.getStatus() == Status.RUNNING) {
          timeline.playFromStart();
        }
        return;
      }
    }

    final StackPane stackPane = new StackPane() {{
      getChildren().add(node);
      setPrefWidth(playbackStateOverlay.getWidth() - playbackStateOverlay.getInsets().getLeft() - playbackStateOverlay.getInsets().getRight());
    }};

    node.opacityProperty().set(0);

    final Group group = new Group(stackPane);
    group.setId(node.getId());
    stackPane.setScaleY(0.0);

    final EventHandler<ActionEvent> shrinkFinished = new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        playbackStateOverlay.getChildren().remove(group);
      }
    };

    final Timeline shrinkTimeline = new Timeline(
      new KeyFrame(Duration.seconds(0.25), shrinkFinished, new KeyValue(stackPane.scaleYProperty(), 0))
    );

    final EventHandler<ActionEvent> fadeInOutFinished = new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        group.setId(null);
        shrinkTimeline.play();
      }
    };

    final Timeline fadeInOutTimeline = new Timeline(
      new KeyFrame(Duration.seconds(0.5), new KeyValue(node.opacityProperty(), 1.0)),
      new KeyFrame(Duration.seconds(2.5), new KeyValue(node.opacityProperty(), 1.0)),
      new KeyFrame(Duration.seconds(3.0), fadeInOutFinished, new KeyValue(node.opacityProperty(), 0.0))
    );


    EventHandler<ActionEvent> expansionFinished = new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        fadeInOutTimeline.play();
      }
    };

    Timeline expansionTimeline = new Timeline(
      new KeyFrame(Duration.seconds(0.25), expansionFinished, new KeyValue(stackPane.scaleYProperty(), 1.0))
    );

    group.setUserData(fadeInOutTimeline);

    playbackStateOverlay.getChildren().add(group);

    expansionTimeline.play();
  }
}
