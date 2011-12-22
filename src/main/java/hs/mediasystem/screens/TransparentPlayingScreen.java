package hs.mediasystem.screens;

import hs.mediasystem.ImageCache;
import hs.mediasystem.ProgramController;
import hs.mediasystem.SizeFormatter;
import hs.mediasystem.StringConverter;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Subtitle;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class TransparentPlayingScreen {
  private final ProgramController controller;
  private final StackPane stackPane = new StackPane();
  private final BorderPane borderPane = new BorderPane();
  private final BorderPane bottomPane = new BorderPane();
  private final Label topLabel = new Label();
  
  private final ObjectProperty<String> volumeText = new SimpleObjectProperty<>();
  private final DoubleProperty volume = new SimpleDoubleProperty(0.0);
  private final LongProperty position = new SimpleLongProperty();
  private final LongProperty length = new SimpleLongProperty(1);
  private final StringProperty osdLine = new SimpleStringProperty("");

  private final Timeline osdFade = new Timeline(
    new KeyFrame(Duration.seconds(1), new KeyValue(topLabel.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(6), new KeyValue(topLabel.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(9), new KeyValue(topLabel.opacityProperty(), 0.0))
  );
  
  private final Timeline fadeIn = new Timeline(
    //new KeyFrame(Duration.seconds(0), new KeyValue(borderPane.opacityProperty(), 0.0)),
    new KeyFrame(Duration.seconds(1), new KeyValue(bottomPane.opacityProperty(), 1.0))
  );
  
  private final Timeline sustainAndFadeOut = new Timeline(
    new KeyFrame(Duration.seconds(0), new KeyValue(bottomPane.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(5), new KeyValue(bottomPane.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(8), new KeyValue(bottomPane.opacityProperty(), 0.0))
  );

  private final Timeline positionUpdater = new Timeline(
    new KeyFrame(Duration.seconds(0.10), new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
//        position.set(SizeFormatter.SECONDS_AS_POSITION.format(controller.getPosition() / 1000));
//        length.set(SizeFormatter.SECONDS_AS_POSITION.format(controller.getLength() / 1000));
        volume.set(controller.getVolume() / 100.0);
        position.set(controller.getPosition());
        long len = controller.getLength();
        
        if(len == 0) {
          len = 1;
        }
        length.set(len);
      }
    })
  );
  
  public TransparentPlayingScreen(ProgramController controller) {
    this.controller = controller;
    
    fadeIn.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        sustainAndFadeOut.play();
      }
    });
    
    positionUpdater.setCycleCount(Timeline.INDEFINITE);
    positionUpdater.play();
  }
  
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);
  
  public Node create(final MediaItem mediaItem, final double w, final double h) {
    volumeText.set("Volume " + controller.getVolume() + "%");
    
    stackPane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if(code == KeyCode.S) {
          controller.stop();
        }
        else if(code == KeyCode.J) {
          Subtitle subtitle = controller.nextSubtitle();
          if(subtitle != null) {
            osdLine.set("Subtitle: " + subtitle.getDescription());
          }
          osdFade.playFromStart();
        }
        else if(BACK_SPACE.match(event)) {
          if(stackPane.getChildren().size() > 1) {
            stackPane.getChildren().remove(1);
            borderPane.requestFocus();
          }
        }
        else if(code == KeyCode.O) {
          if(stackPane.getChildren().size() == 1) {
            ObservableList<Option> options = FXCollections.observableArrayList(
              new NumericOption(controller.getPlayer().volumeProperty(), "Volume", "%3.0f%%", 1, 0, 100),
              new ListOption<Subtitle>("Subtitle", controller.getPlayer().subtitleProperty(), controller.getPlayer().getSubtitles(), new StringConverter<Subtitle>() {
                @Override
                public String toString(Subtitle object) {
                  return object.getDescription();
                }
              }),
              new ListOption<AudioTrack>("Audio Track", controller.getPlayer().audioTrackProperty(), controller.getPlayer().getAudioTracks(), new StringConverter<AudioTrack>() {
                @Override
                public String toString(AudioTrack object) {
                  return object.getDescription();
                }
              }),
              new NumericOption(controller.getPlayer().rateProperty(), "Playback Speed", "%4.1f", 0.1, 0.1, 4.0),
              new NumericOption(controller.getPlayer().audioDelayProperty(), "Audio Delay", "%5.0fms", 100, -30000, 30000), 
              new NumericOption(controller.getPlayer().brightnessProperty(), "Brightness", "%4.1f", 0.1, 0, 2) 
            );
            
            stackPane.getChildren().add(new DialogScreen("Video - Options", options));
            
            // HACK Using setDisable to shift the focus to the Options Dialog TODO this doesn't even always work...
            borderPane.setDisable(true);
            Platform.runLater(new Runnable() {
              @Override
              public void run() {
                borderPane.setDisable(false);
              }
            });
          }
        }
        else if(code == KeyCode.SPACE) {
          controller.pause();
          showOSD();
        }
        else if(code == KeyCode.NUMPAD4) {
          controller.move(-10 * 1000);
          showOSD();
        }
        else if(code == KeyCode.NUMPAD6) {
          controller.move(10 * 1000);
          showOSD();
        }
        else if(code == KeyCode.NUMPAD2) {
          controller.move(-60 * 1000);
          showOSD();
        }
        else if(code == KeyCode.NUMPAD8) {
          controller.move(60 * 1000);
          showOSD();
        }
        else if(code == KeyCode.M) {
          controller.mute();
          showOSD();
        }
        else if(code == KeyCode.DIGIT9) {
          controller.changeVolume(-1);
          volumeText.set("Volume " + controller.getVolume() + "%");
          showOSD();
        }
        else if(code == KeyCode.DIGIT0) {
          controller.changeVolume(1);
          volumeText.set("Volume " + controller.getVolume() + "%");
          showOSD();
        }
        else if(code == KeyCode.DIGIT1) {
          controller.changeBrightness(-0.05f);
        }
        else if(code == KeyCode.DIGIT2) {
          controller.changeBrightness(0.05f);
        }
        else if(code == KeyCode.Z) {
          controller.changeSubtitleDelay(-100);
        }
        else if(code == KeyCode.X) {
          controller.changeSubtitleDelay(100);
        }       
      }
    });
    
    borderPane.setFocusTraversable(true);
    borderPane.setTop(topLabel);
    
    topLabel.setId("video-osd-line");
    topLabel.textProperty().bind(osdLine);
    
    borderPane.setBottom(bottomPane);
    
    bottomPane.setId("video-overlay");
    bottomPane.setLeft(new ImageView(ImageCache.loadImage(mediaItem.getPoster())) {{
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
          getChildren().add(new Label(mediaItem.getTitle()) {{
            getStyleClass().add("video-title");
            setEffect(createEffect(64));
          }});
          getChildren().add(new Label(mediaItem.getSubtitle()) {{
            getStyleClass().add("video-subtitle");
          }});
          getChildren().add(new GridPane() {{
//              setSpacing(20);
            setHgap(20);
            getColumnConstraints().addAll(
              new ColumnConstraints() {{
              }},
              new ColumnConstraints() {{
                setPercentWidth(60.0);   // this is not working as I want it.
                setHalignment(HPos.RIGHT);
              }},
              new ColumnConstraints() {{
                setPercentWidth(20.0);
              }},
              new ColumnConstraints() {{
              }}
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
              getStyleClass().add("position");
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
              getStyleClass().add("volume");
              progressProperty().bind(volume);
              setMaxWidth(100000);
              HBox.setHgrow(this, Priority.ALWAYS);
            }}, 2, 1);
            add(new Label("+"), 3, 1);
          }});            
        }});
//          getChildren().add(new Slider() {{
//            setOrientation(Orientation.VERTICAL);
//          }});
      }});
    }});
    
    stackPane.getChildren().add(borderPane);
    
    stackPane.sceneProperty().addListener(new ChangeListener<Scene>() {
      @Override
      public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
        if(newValue != null) {
          sustainAndFadeOut.playFromStart();
        }
      }
    });
    
    return stackPane;
  }

  private void showOSD() {
    if(fadeIn.getStatus() == Status.STOPPED && sustainAndFadeOut.getStatus() == Status.STOPPED) {
      fadeIn.play();
    }
    else if(fadeIn.getStatus() == Status.STOPPED && sustainAndFadeOut.getStatus() == Status.RUNNING) {
      sustainAndFadeOut.playFromStart();
    }
  }
  
  private static Effect createEffect(final double size) { // font point size
    return new Blend() {{
      setMode(BlendMode.MULTIPLY);
      setBottomInput(new DropShadow() {{
        setColor(Color.rgb(254, 235, 66, 0.3));
        setOffsetX(size / 22);
        setOffsetY(size / 22);
        setSpread(0.2);
      }});
      setTopInput(new Blend() {{
        setMode(BlendMode.MULTIPLY);
        setBottomInput(new DropShadow() {{
          setColor(Color.web("#f13a00"));
          setRadius(size / 5.5);
          setSpread(0.2);
        }});
        setTopInput(new Blend() {{
          setMode(BlendMode.MULTIPLY);
          setBottomInput(new InnerShadow() {{
            setColor(Color.web("#feeb42"));
            setRadius(size / 12);
            setChoke(0.8);
          }});
          setTopInput(new InnerShadow() {{
            setColor(Color.web("#f13a00"));
            setRadius(size / 22);
            setChoke(0.4);
          }});
        }});
      }});
    }};
  }
}
