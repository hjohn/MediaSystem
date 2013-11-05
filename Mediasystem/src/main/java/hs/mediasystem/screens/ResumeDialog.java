package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.DialogStage;
import hs.mediasystem.util.SceneUtil;
import hs.mediasystem.util.SizeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ResumeDialog extends DialogStage<Void> {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final MediaItem mediaItem;
  private final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent event) {
      MediaData mediaData = mediaItem.mediaData.get();

      if(mediaData != null) {
        foundMediaData(mediaData);
      }
    }
  }));

  private final VBox root = new VBox();

  private int resumePosition;
  private boolean wasCancelled;
  private boolean resumePositionFound;

  public ResumeDialog(MediaItem mediaItem) {
    this.mediaItem = mediaItem;

    setScene(SceneUtil.createScene(root));

    root.getStylesheets().add("collection/resume-dialog.css");
    root.getStyleClass().add("resume-dialog");
    root.getChildren().add(new ProgressIndicator());
    root.setFillWidth(true);

    getScene().setFill(Color.TRANSPARENT);
    getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          wasCancelled = true;
          close();
        }
      }
    });
  }

  private void foundMediaData(final MediaData mediaData) {
    timeline.stop();

    if(mediaData.resumePosition.get() > 0) {
      resumePositionFound = true;

      root.getChildren().clear();
      root.getChildren().add(new Button("Resume from " + SizeFormatter.SECONDS_AS_POSITION.format(mediaData.resumePosition.get())) {{
        setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            resumePosition = mediaData.resumePosition.get();
            close();
          }
        });
        setMaxWidth(10000);
      }});
      root.getChildren().add(new Button("Start from beginning") {{
        setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            close();
          }
        });
        setMaxWidth(10000);
      }});

      recenter();
    }
    else {
      close();
    }
  }

  @Override
  protected void onShow() {
    timeline.setCycleCount(40);
    timeline.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        if(!resumePositionFound) {
          close();
        }
      }
    });
    timeline.play();
  }

  public boolean wasCancelled() {
    return wasCancelled;
  }

  public int getResumePosition() {
    return resumePosition;
  }
}
