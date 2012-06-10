package hs.mediasystem.screens;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.PlaybackOverlayView;
import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;
import hs.mediasystem.util.SpecialEffects;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.effect.Blend;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Reflection;
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

import org.tbee.javafx.scene.layout.MigPane;

public class PlaybackOverlayPane extends StackPane implements PlaybackOverlayView {
  private final ObjectProperty<MediaItem> mediaItem = new SimpleObjectProperty<>();
  @Override public ObjectProperty<MediaItem> mediaItemProperty() { return mediaItem; }

  private final ObjectProperty<Player> player = new SimpleObjectProperty<>();
  @Override public ObjectProperty<Player> playerProperty() { return player; }

  private final PlayerBindings playerBindings = new PlayerBindings(player);

  private final MigPane detailsOverlay = new MigPane("fill", "[5%!][20%!][5%!][65%!][5%!]", "[45%!][50%!][5%!]");

  private final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(mediaItem, "dataMap", Media.class, "image");
  private final AsyncImageProperty poster = new AsyncImageProperty();

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

    setFocusTraversable(true);

    detailsOverlay.setId("video-overlay");
    detailsOverlay.add(new ScaledImageView() {{
      imageProperty().bind(poster);
      setAlignment(Pos.BOTTOM_RIGHT);
      getStyleClass().add("poster");
      setEffect(new Blend() {{
        setBottomInput(new DropShadow());
        setTopInput(new Reflection() {{
          this.setFraction(0.10);
        }});
      }});
    }}, "cell 1 1, grow, bottom, left");
    detailsOverlay.add(new BorderPane() {{
      setId("video-overlay_info");
      setBottom(new HBox() {{
        getChildren().add(new VBox() {{
          HBox.setHgrow(this, Priority.ALWAYS);
          getChildren().add(new Label() {{
            textProperty().bind(MapBindings.selectString(mediaItem, "dataMap", Media.class, "title"));
            getStyleClass().add("video-title");
            setEffect(SpecialEffects.createNeonEffect(64));
          }});
          getChildren().add(new Label() {{
            textProperty().bind(MapBindings.selectString(mediaItem, "dataMap", Media.class, "subtitle"));
            getStyleClass().add("video-subtitle");
          }});
          getChildren().add(new GridPane() {{
//              setSpacing(20);
            setHgap(20);
            getColumnConstraints().addAll(
              new ColumnConstraints(),
              new ColumnConstraints() {{
                setPercentWidth(60.0);   // this is not working as I want it.
                setHalignment(HPos.RIGHT);
              }},
              new ColumnConstraints() {{
                setPercentWidth(20.0);
              }},
              new ColumnConstraints()
            );
            setId("video-overlay_info_bar");
            add(new Label() {{
              textProperty().bind(playerBindings.formattedPosition);
            }}, 0, 0);
            add(new ProgressBar(0) {{
              getStyleClass().add("orange-bar");
              progressProperty().bind(Bindings.divide(Bindings.add(playerBindings.position, 0.0), playerBindings.length));
              setMaxWidth(100000);
              HBox.setHgrow(this, Priority.ALWAYS);
            }}, 1, 0, 2, 1);
            add(new Label() {{
              textProperty().bind(playerBindings.formattedLength);
            }}, 3, 0);
            add(new Label("-"), 1, 1);
            add(new ProgressBar(0) {{
              getStyleClass().add("red-bar");
              progressProperty().bind(playerBindings.volume);
              setMaxWidth(100000);
              HBox.setHgrow(this, Priority.ALWAYS);
            }}, 2, 1);
            add(new Label("+"), 3, 1);
          }});
        }});
      }});
    }}, "cell 3 1, growx, bottom");

    getChildren().add(detailsOverlay);

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

    sceneProperty().addListener(new ChangeListener<Scene>() {
      @Override
      public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
        if(newValue != null) {
          fadeInSustainAndFadeOut.playFromStart();
        }
      }
    });

    playerBindings.position.addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if(Math.abs(oldValue.longValue() - newValue.longValue()) > 2500) {
          addOSD(createOSDItem("Position", 0.0, 100.0, playerBindings.position.multiply(100.0).divide(playerBindings.length), playerBindings.formattedPosition));
        }
      }
    });

    playerBindings.volume.addListener(new FirstChangeFilter<>(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        addOSD(createOSDItem("Volume", 0.0, 100.0, playerBindings.volume, playerBindings.formattedVolume));
      }
    }));

    playerBindings.rate.addListener(new FirstChangeFilter<>(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        addOSD(createOSDItem("Playback Speed", 0.0, 4.0, playerBindings.rate, playerBindings.formattedRate));
      }
    }));

    playerBindings.audioDelay.addListener(new FirstChangeFilter<>(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        addOSD(createOSDItem("Audio Delay", -120.0, 120.0, playerBindings.audioDelay.divide(1000.0), playerBindings.formattedAudioDelay));
      }
    }));

    playerBindings.subtitleDelay.addListener(new FirstChangeFilter<>(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        addOSD(createOSDItem("Subtitle Delay", -120.0, 120.0, playerBindings.subtitleDelay.divide(1000.0), playerBindings.formattedSubtitleDelay));
      }
    }));

    playerBindings.brightness.addListener(new FirstChangeFilter<>(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        addOSD(createOSDItem("Brightness Adjustment", -100.0, 100.0, playerBindings.brightness.subtract(1.0).multiply(100.0), playerBindings.formattedBrightness));
      }
    }));

    playerBindings.audioTrack.addListener(new FirstChangeFilter<>(new ChangeListener<AudioTrack>() {
      @Override
      public void changed(ObservableValue<? extends AudioTrack> observable, AudioTrack oldValue, AudioTrack value) {
        addOSD(createOSDItem("Audio Track", playerBindings.formattedAudioTrack));
      }
    }));

    playerBindings.subtitle.addListener(new FirstChangeFilter<>(new ChangeListener<Subtitle>() {
      @Override
      public void changed(ObservableValue<? extends Subtitle> observable, Subtitle oldValue, Subtitle value) {
        addOSD(createOSDItem("Subtitle", playerBindings.formattedSubtitle));
      }
    }));

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

  private Node createOSDItem(final String title, final StringExpression valueText) {
    return new VBox() {{
      setId(title);
      getStyleClass().add("item");
      getChildren().add(new BorderPane() {{
        setLeft(new Label(title) {{
          getStyleClass().add("title");
        }});
        setRight(new Label() {{
          getStyleClass().add("value");
          textProperty().bind(valueText);
        }});
      }});
    }};
  }

  private Node createOSDItem(final String title, final double min, final double max, final NumberExpression value, final StringExpression valueText) {
    return new VBox() {{
      setId(title);
      getStyleClass().add("item");
      getChildren().add(new BorderPane() {{
        setLeft(new Label(title) {{
          getStyleClass().add("title");
        }});
        setRight(new Label() {{
          getStyleClass().add("value");
          textProperty().bind(valueText);
        }});
      }});
      getChildren().add(new Slider(min, max * 1.01, 0) {{  // WORKAROUND: 1.01 to work around last label display bug
        valueProperty().bind(value);
        setMinorTickCount(4);
        setMajorTickUnit(max / 4);
      }});
    }};
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

  private static class FirstChangeFilter<T> implements ChangeListener<T> {
    private final ChangeListener<T> changeListener;

    private boolean notFirst;

    public FirstChangeFilter(ChangeListener<T> changeListener) {
      this.changeListener = changeListener;
    }

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T value) {
      if(notFirst) {
        changeListener.changed(observable, oldValue, value);
      }

      notFirst = true;
    }
  }
}
