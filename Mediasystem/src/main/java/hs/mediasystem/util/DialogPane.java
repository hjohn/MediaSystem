package hs.mediasystem.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DialogPane extends StackPane implements Dialog {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final StackPane stackPane = new StackPane();

  private Stage owner;

  public DialogPane() {
    getStylesheets().add("dialog/dialog.css");
    getStyleClass().add("dialog");

    setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          event.consume();
          close();
        }
      }
    });
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

   // this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

    stackPane.getChildren().add(root);
    stackPane.getChildren().add(this);

    StackPane.setMargin(this, new Insets(40, 40, 40, 40));

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
