package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.DialogPane;
import hs.mediasystem.util.SizeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ResumeDialog extends DialogPane<Integer> {
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

  private Integer resumePosition;
  private boolean resumePositionFound;

  public ResumeDialog(MediaItem mediaItem) {
    this.mediaItem = mediaItem;

    getStylesheets().add("collection/resume-dialog.css");
    getStyleClass().add("resume-dialog");

    root.getChildren().add(new ProgressIndicator());  // Shown in case media data is slow to load

    getChildren().add(root);
  }

  private void foundMediaData(final MediaData mediaData) {
    timeline.stop();

    if(mediaData.resumePosition.get() > 0) {
      resumePositionFound = true;

      Button resumeButton = new Button("Resume from " + SizeFormatter.SECONDS_AS_POSITION.format(mediaData.resumePosition.get())) {{
        setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            resumePosition = mediaData.resumePosition.get();
            close();
          }
        });
        setMaxWidth(10000);
      }};

      root.getChildren().clear();
      root.getChildren().add(resumeButton);
      root.getChildren().add(new Button("Start from beginning") {{
        setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            resumePosition = 0;
            close();
          }
        });
        setMaxWidth(10000);
      }});

      resumeButton.requestFocus();
    }
    else {
      resumePosition = 0;
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

  @Override
  protected Integer getResult() {
    return resumePosition;
  }
}
