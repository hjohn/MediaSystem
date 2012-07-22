package hs.mediasystem.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DialogPane extends StackPane implements Dialog {
  private final StackPane stackPane = new StackPane();

  private Stage owner;

  public DialogPane() {
    getStylesheets().add("dialog/dialog.css");
    getStyleClass().add("dialog");
  }

  protected void setParentEffect(Parent parent) {
    ColorAdjust colorAdjust = new ColorAdjust();

    Timeline fadeOut = new Timeline(
      new KeyFrame(Duration.ZERO,
        new KeyValue(colorAdjust.brightnessProperty(), 0)
      ),
      new KeyFrame(Duration.seconds(1),
        new KeyValue(colorAdjust.brightnessProperty(), -0.5)
      )
    );

    parent.setEffect(colorAdjust);

    fadeOut.play();
  }

  protected void removeParentEffect(Parent parent) {
    parent.setEffect(null);
  }

  @Override
  public void showDialog(Stage parentStage, boolean synchronous) {
    if(synchronous) {
      throw new UnsupportedOperationException();
    }

    this.owner = parentStage;

    Parent root = parentStage.getScene().getRoot();
    parentStage.getScene().setRoot(stackPane);

    this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

    stackPane.getChildren().add(root);
    stackPane.getChildren().add(this);

    setParentEffect(root);
    requestFocus();
  }

  public void close() {
    Parent originalRoot = (Parent)stackPane.getChildren().remove(0);

    owner.getScene().setRoot(originalRoot);
    removeParentEffect(originalRoot);

    owner = null;
  }

  @Override
  public void requestFocus() {
    getChildren().get(0).requestFocus();
  }
}
