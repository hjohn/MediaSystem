package hs.mediasystem.util;

import com.sun.javafx.tk.Toolkit;

import hs.mediasystem.screens.NavigationEvent;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DialogPane<R> extends StackPane implements Dialog<R> {
  private final StackPane stackPane = new StackPane();

  private Stage owner;
  private Node oldFocusOwner;
  private boolean synchronous;

  public DialogPane() {
    getStylesheets().add("dialog/dialog.css");
    getStyleClass().add("dialog");

    setFocusTraversable(true);

    addEventHandler(NavigationEvent.NAVIGATION_ANCESTOR, new EventHandler<NavigationEvent>() {
      @Override
      public void handle(NavigationEvent event) {
        close();

        if(event.getEventType() == NavigationEvent.NAVIGATION_BACK) {
          event.consume();
        }
      }
    });

    setMaxWidth(Region.USE_PREF_SIZE);
    setMaxHeight(Region.USE_PREF_SIZE);
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

  /**
   * This method can be overriden to provide the result of this dialog when it is
   * closed.
   *
   * @return the result of this dialog
   */
  protected R getResult() {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public R showDialog(Stage parentStage, boolean synchronous) {
    this.synchronous = synchronous;
    this.owner = parentStage;
    this.oldFocusOwner = parentStage.getScene().getFocusOwner();

    StackPane.setMargin(this, new Insets(40, 40, 40, 40));

    Parent root = parentStage.getScene().getRoot();

    stackPane.getChildren().add(root);
    stackPane.getChildren().add(this);

    parentStage.getScene().setRoot(stackPane);

    onShow();
    setParentEffect(root);
    requestFocus();

    if(synchronous) {
      return (R)Toolkit.getToolkit().enterNestedEventLoop(this);
    }

    return null;
  }

  public void close() {
    Parent originalRoot = (Parent)stackPane.getChildren().remove(0);

    owner.getScene().setRoot(originalRoot);
    removeParentEffect(originalRoot);

    owner = null;

    if(oldFocusOwner != null) {
      oldFocusOwner.requestFocus();
    }

    if(synchronous) {
      Toolkit.getToolkit().exitNestedEventLoop(this, getResult());
    }
  }

  protected void onShow() {
  }

  @Override
  public void requestFocus() {
    Node initialFocusNode = lookup(".initial-focus");

    if(initialFocusNode != null) {
      initialFocusNode.requestFocus();
    }
    else {
      super.requestFocus();
    }
  }
}
