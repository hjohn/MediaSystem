package hs.mediasystem.util;

import hs.mediasystem.screens.NavigationEvent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DialogPane extends StackPane implements Dialog {
  private final StackPane stackPane = new StackPane();

  private Stage owner;
  private Node oldFocusOwner;

  public DialogPane() {
    getStylesheets().add("dialog/dialog.css");
    getStyleClass().add("dialog");

    addEventHandler(NavigationEvent.NAVIGATION_ANCESTOR, new EventHandler<NavigationEvent>() {
      @Override
      public void handle(NavigationEvent event) {
        close();

        if(event.getEventType() == NavigationEvent.NAVIGATION_BACK) {
          event.consume();
        }
      }
    });
  }

  protected void setParentEffect(Parent parent) {
    ColorAdjust colorAdjust = new ColorAdjust();

    parent.setDisable(true);

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
    parent.setDisable(false);
    parent.setEffect(null);
  }

  @Override
  public void showDialog(Stage parentStage, boolean synchronous) {
    if(synchronous) {
      throw new UnsupportedOperationException();
    }

    this.owner = parentStage;
    this.oldFocusOwner = parentStage.getScene().getFocusOwner();

    StackPane.setMargin(this, new Insets(40, 40, 40, 40));

    Parent root = parentStage.getScene().getRoot();

    stackPane.getChildren().add(root);
    stackPane.getChildren().add(this);

    parentStage.getScene().setRoot(stackPane);

    setParentEffect(root);
    requestFocus();
  }

  public void close() {
    Parent originalRoot = (Parent)stackPane.getChildren().remove(0);

    owner.getScene().setRoot(originalRoot);
    removeParentEffect(originalRoot);

    owner = null;

    if(oldFocusOwner != null) {
      oldFocusOwner.requestFocus();
    }
  }

  @Override
  public void requestFocus() {
    Node initialFocusNode = lookup(".initial-focus");

    if(initialFocusNode != null) {
      initialFocusNode.requestFocus();
    }
  }
}
