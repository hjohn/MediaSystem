package hs.mediasystem.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.effect.ColorAdjust;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class DialogStage extends Stage {

  public DialogStage() {
    super(StageStyle.TRANSPARENT);

    this.setTitle("MediaSystem-dialog");

    initModality(Modality.APPLICATION_MODAL);
  }

  protected void setParentEffect(Stage parent) {
    ColorAdjust colorAdjust = new ColorAdjust();

    Timeline fadeOut = new Timeline(
      new KeyFrame(Duration.ZERO,
        new KeyValue(colorAdjust.brightnessProperty(), 0)
      ),
      new KeyFrame(Duration.seconds(1),
        new KeyValue(colorAdjust.brightnessProperty(), -0.5)
      )
    );

    parent.getScene().getRoot().setEffect(colorAdjust);

    fadeOut.play();
  }

  protected void removeParentEffect(Stage parent) {
    parent.getScene().getRoot().setEffect(null);
  }

  protected void recenter() {
    Window parent = getOwner();

    sizeToScene();

    setX(parent.getX() + parent.getWidth() / 2 - DialogStage.this.getWidth() / 2);
    setY(parent.getY() + parent.getHeight() / 2 - DialogStage.this.getHeight() / 2);
  }

  public final void showDialog(final Stage parent, boolean synchronous) {
    initOwner(parent);

    setParentEffect(parent);

    setOnShown(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        recenter();
        onShow();
      }
    });

    if(synchronous) {
      showAndWait();
    }
    else {
      show();
    }
  }

  protected void onShow() {
  }

  @Override
  public void close() {
    removeParentEffect((Stage)getOwner());
    super.close();
  }
}
