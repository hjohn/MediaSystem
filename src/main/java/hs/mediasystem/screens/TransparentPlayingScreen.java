package hs.mediasystem.screens;

import hs.mediasystem.ImageCache;
import hs.mediasystem.ProgramController;
import hs.mediasystem.SizeFormatter;
import hs.mediasystem.framework.MediaItem;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class TransparentPlayingScreen {
  private final ProgramController controller;
  private final BorderPane borderPane = new BorderPane();
  private final ObjectProperty<String> volume = new SimpleObjectProperty<>();
  private final LongProperty position = new SimpleLongProperty();
  private final LongProperty length = new SimpleLongProperty(1);

  private final Timeline fadeIn = new Timeline(
    //new KeyFrame(Duration.seconds(0), new KeyValue(borderPane.opacityProperty(), 0.0)),
    new KeyFrame(Duration.seconds(1), new KeyValue(borderPane.opacityProperty(), 1.0))
  );
  
  private final Timeline sustainAndFadeOut = new Timeline(
    new KeyFrame(Duration.seconds(0), new KeyValue(borderPane.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(5), new KeyValue(borderPane.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(8), new KeyValue(borderPane.opacityProperty(), 0.0))
  );

  private final Timeline positionUpdater = new Timeline(
    new KeyFrame(Duration.seconds(0.10), new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
//        position.set(SizeFormatter.SECONDS_AS_POSITION.format(controller.getPosition() / 1000));
//        length.set(SizeFormatter.SECONDS_AS_POSITION.format(controller.getLength() / 1000));
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
  
  public Node create(final MediaItem mediaItem, final double w, final double h) {
    volume.set("Volume " + controller.getVolume() + "%");
    
    borderPane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if(code == KeyCode.S) {
          controller.stop();
        }
        else if(code == KeyCode.SPACE) {
          controller.pause();
        }
        else if(code == KeyCode.NUMPAD4) {
          controller.move(-10 * 1000);
        }
        else if(code == KeyCode.NUMPAD6) {
          controller.move(10 * 1000);
        }
        else if(code == KeyCode.NUMPAD2) {
          controller.move(-60 * 1000);
        }
        else if(code == KeyCode.NUMPAD8) {
          controller.move(60 * 1000);
        }
        else if(code == KeyCode.M) {
          controller.mute();
        }
        else if(code == KeyCode.DIGIT9) {
          controller.changeVolume(-1);
          volume.set("Volume " + controller.getVolume() + "%");
        }
        else if(code == KeyCode.DIGIT0) {
          controller.changeVolume(1);
          volume.set("Volume " + controller.getVolume() + "%");
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
        
        showOSD();
      }
    });
    
    borderPane.setFocusTraversable(true);
    borderPane.setTop(new Label() {{
      textProperty().bind(volume);
    }});
    
    borderPane.setBottom(new BorderPane() {{
      setId("video-overlay");
      setLeft(new ImageView(ImageCache.loadImage(mediaItem.getPoster())) {{
        getStyleClass().add("poster");
        setFitWidth(w * 0.2);
        setFitHeight(h * 0.4);
        setPreserveRatio(true);
        setEffect(new Blend() {{
          setBottomInput(new DropShadow());
          setTopInput(new Reflection());
        }});
      }});
      setCenter(new BorderPane() {{
        setId("video-overlay_info");
        setBottom(new VBox() {{
          getChildren().add(new Label(mediaItem.getTitle()) {{
            getStyleClass().add("video-title");
            setEffect(createEffect(64));
          }});
          getChildren().add(new Label(mediaItem.getSubtitle()) {{
            getStyleClass().add("video-subtitle");
          }});
          getChildren().add(new HBox() {{
            setId("video-overlay_info_bar");
            getChildren().add(new Label() {{
              textProperty().bind(new StringBinding() {
                {
                  bind(position);
                }
                
                @Override
                protected String computeValue() {
                  return SizeFormatter.SECONDS_AS_POSITION.format(position.get() / 1000);
                }
              });
            }});
            getChildren().add(new ProgressBar(0) {{
              progressProperty().bind(Bindings.divide(Bindings.add(position, 0.0), length));
              setMaxWidth(100000);
              HBox.setHgrow(this, Priority.ALWAYS);
            }});
            getChildren().add(new Label() {{
              textProperty().bind(new StringBinding() {
                {
                  bind(length);
                }
                
                @Override
                protected String computeValue() {
                  return SizeFormatter.SECONDS_AS_POSITION.format(length.get() / 1000);
                }
              });
            }});
          }});
        }});
      }});
    }});
    
    return borderPane;
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
