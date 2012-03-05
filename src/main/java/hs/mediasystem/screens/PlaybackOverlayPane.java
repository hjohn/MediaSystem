package hs.mediasystem.screens;

import hs.mediasystem.util.SizeFormatter;
import hs.mediasystem.util.SpecialEffects;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.Blend;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PlaybackOverlayPane extends StackPane {
  private final BorderPane borderPane = new BorderPane();
  private final BorderPane bottomPane = new BorderPane();

  private final VBox playbackStateOverlay = new VBox() {{
    getStylesheets().add("playback-state-overlay.css");
    getStyleClass().add("content-box");
    setVisible(false);
  }};

  private final Timeline fadeInSustainAndFadeOut = new Timeline(
    new KeyFrame(Duration.seconds(0)),
    new KeyFrame(Duration.seconds(1), new KeyValue(bottomPane.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(6), new KeyValue(bottomPane.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(9), new KeyValue(bottomPane.opacityProperty(), 0.0))
  );

  public PlaybackOverlayPane() {
    getStylesheets().add("playback-overlay.css");
    setId("playback-overlay");

    final double w = 1920;  // TODO remove hardcoded values
    final double h = 1200;

    playbackStateOverlay.getChildren().addListener(new ListChangeListener<Node>() {
      @Override
      public void onChanged(ListChangeListener.Change<? extends Node> change) {
        playbackStateOverlay.setVisible(!change.getList().isEmpty());
      }
    });

    borderPane.setFocusTraversable(true);
    borderPane.setBottom(bottomPane);

    bottomPane.setId("video-overlay");
    bottomPane.setLeft(new ImageView() {{
      imageProperty().bind(poster);
      getStyleClass().add("poster");
      setFitWidth(w * 0.2);
      setFitHeight(h * 0.4);
      setPreserveRatio(true);
      setEffect(new Blend() {{
        setBottomInput(new DropShadow());
        setTopInput(new Reflection() {{
          this.setFraction(0.10);
        }});
      }});
    }});
    bottomPane.setCenter(new BorderPane() {{
      setId("video-overlay_info");
      setBottom(new HBox() {{
        getChildren().add(new VBox() {{
          HBox.setHgrow(this, Priority.ALWAYS);
          getChildren().add(new Label() {{
            textProperty().bind(title);
            getStyleClass().add("video-title");
            setEffect(SpecialEffects.createNeonEffect(64));
          }});
          getChildren().add(new Label() {{
            textProperty().bind(subtitle);
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
              textProperty().bind(new StringBinding() {
                {
                  bind(position);
                }

                @Override
                protected String computeValue() {
                  return SizeFormatter.SECONDS_AS_POSITION.format(position.get() / 1000);
                }
              });
            }}, 0, 0);
            add(new ProgressBar(0) {{
              getStyleClass().add("orange-bar");
              progressProperty().bind(Bindings.divide(Bindings.add(position, 0.0), length));
              setMaxWidth(100000);
              HBox.setHgrow(this, Priority.ALWAYS);
            }}, 1, 0, 2, 1);
            add(new Label() {{
              textProperty().bind(new StringBinding() {
                {
                  bind(length);
                }

                @Override
                protected String computeValue() {
                  return SizeFormatter.SECONDS_AS_POSITION.format(length.get() / 1000);
                }
              });
            }}, 3, 0);
            add(new Label("-"), 1, 1);
            add(new ProgressBar(0) {{
              getStyleClass().add("red-bar");
              progressProperty().bind(volume);
              setMaxWidth(100000);
              HBox.setHgrow(this, Priority.ALWAYS);
            }}, 2, 1);
            add(new Label("+"), 3, 1);
          }});
        }});
      }});
    }});

    getChildren().add(borderPane);

    getChildren().add(new GridPane() {{
      getColumnConstraints().add(new ColumnConstraints() {{
        setPercentWidth(25);
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

      add(playbackStateOverlay, 1, 1);
    }});

    sceneProperty().addListener(new ChangeListener<Scene>() {
      @Override
      public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
        if(newValue != null) {
          fadeInSustainAndFadeOut.playFromStart();
        }
      }
    });
  }

  public void showOSD() {
    fadeInSustainAndFadeOut.playFromStart();
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

  private final DoubleProperty volume = new SimpleDoubleProperty(0);
  public double getVolume() { return volume.get(); }
  public void setVolume(double volume) { this.volume.set(volume); }
  public DoubleProperty volumeProperty() { return volume; }

  private final LongProperty position = new SimpleLongProperty();
  public long getPosition() { return position.get(); }
  public void setPosition(long position) { this.position.set(position); }
  public LongProperty positionProperty() { return position; }

  private final LongProperty length = new SimpleLongProperty(1);
  public long getLength() { return length.get(); }
  public void setLength(long length) { this.length.set(length); }
  public LongProperty lengthProperty() { return length; }

  private final StringProperty title = new SimpleStringProperty();
  public String getTitle() { return title.get(); }
  public void setTitle(String title) { this.title.set(title); }
  public StringProperty titleProperty() { return title; }

  private final StringProperty subtitle = new SimpleStringProperty();
  public String getSubtitle() { return subtitle.get(); }
  public void setSubtitle(String subtitle) { this.subtitle.set(subtitle); }
  public StringProperty subtitleProperty() { return subtitle; }

  private final StringProperty releaseYear = new SimpleStringProperty();
  public String getReleaseYear() { return releaseYear.get(); }
  public void setReleaseYear(String releaseYear) { this.releaseYear.set(releaseYear); }
  public StringProperty releaseYearProperty() { return releaseYear; }

  private final ObjectProperty<Image> poster = new SimpleObjectProperty<>();
  public Image getPoster() { return poster.get(); }
  public void setPoster(Image poster) { this.poster.set(poster); }
  public ObjectProperty<Image> posterProperty() { return poster; }

}
