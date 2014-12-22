package hs.mediasystem.util;

import hs.mediasystem.screens.NavigationEvent;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import com.sun.javafx.tk.Toolkit;

public class DialogPane<R> extends StackPane {
  private DialogGlass dialogGlass;
  private boolean synchronous;

  private final EventHandler<NavigationEvent> eventHandler = new EventHandler<NavigationEvent>() {
    @Override
    public void handle(NavigationEvent event) {
      close();

      if(event.getEventType() == NavigationEvent.NAVIGATION_BACK) {
        event.consume();
      }
    }
  };

  public DialogPane() {
    getStylesheets().add("dialog/dialog.css");
    getStyleClass().add("dialog");

    setMaxWidth(Region.USE_PREF_SIZE);
    setMaxHeight(Region.USE_PREF_SIZE);
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
  public R showDialog(Scene scene, boolean synchronous) {
    if(scene == null) {
      throw new IllegalStateException("scene cannot be null");
    }

    this.synchronous = synchronous;

    StackPane.setMargin(this, new Insets(40, 40, 40, 40));

    Parent root = scene.getRoot();

    dialogGlass = new DialogGlass(scene, root);
    dialogGlass.addEventHandler(NavigationEvent.NAVIGATION_ANCESTOR, eventHandler);
    dialogGlass.setDialog(this);

    Event.fireEvent(this, new DialogEvent(true));

    onShow();
    requestFocus();

    if(synchronous) {
      return (R)Toolkit.getToolkit().enterNestedEventLoop(this);
    }

    return null;
  }

  public void close() {
    dialogGlass.removeEventHandler(NavigationEvent.NAVIGATION_ANCESTOR, eventHandler);
    dialogGlass.remove();
    dialogGlass = null;

    Event.fireEvent(this, new DialogEvent(false));

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
  }

  private static class DialogGlass extends StackPane {
    private final ColorAdjust colorAdjust = new ColorAdjust();
    private final Timeline fade;

    private Parent child;
    private Node oldFocusOwner;
    private StackPane fadingPane;

    public DialogGlass(Scene scene, Parent root) {
      this.child = root;

      scene.setRoot(this);

      fadingPane = new StackPane(child);
      fadingPane.setEffect(colorAdjust);

      getChildren().add(fadingPane);

      oldFocusOwner = scene.getFocusOwner();

      fade = new Timeline(
        new KeyFrame(
          Duration.ZERO,
          new KeyValue(colorAdjust.brightnessProperty(), -0.5)
        ),
        new KeyFrame(
          Duration.seconds(1.5),
          "center",
          new KeyValue(colorAdjust.brightnessProperty(), 0)
        ),
        new KeyFrame(
          Duration.seconds(1.51),
          event -> {
            fadingPane.getChildren().clear();

            Parent parent = DialogGlass.this.getParent();

            if(parent != null && parent.getParent() instanceof DialogGlass) {
              ((DialogGlass)parent.getParent()).child = child;
              ((DialogGlass)parent.getParent()).fadingPane.getChildren().setAll(child);
            }
            else {
              child.getStyleClass().remove("root");  // WORKAROUND RT-39159
              scene.setRoot(child);
            }
          },
          new KeyValue(colorAdjust.brightnessProperty(), 0)
        )
      );
    }

    public void setDialog(DialogPane<?> dialogPane) {
      child.getStyleClass().add("enabled-look");
      child.setDisable(true);

      if(getChildren().size() > 1) {
        getChildren().remove(1);
      }

      getChildren().add(dialogPane);

      fade.setRate(-1.0);
      fade.playFrom("center");
    }

    public void remove() {
      child.setDisable(false);
      child.getStyleClass().remove("enabled-look");

      if(getChildren().size() > 1) {
        getChildren().remove(1);
      }

      if(oldFocusOwner != null) {
        oldFocusOwner.requestFocus();
      }

      fade.setRate(3.0);
      if(fade.getStatus() != Animation.Status.RUNNING) {
        fade.playFromStart();
      }
    }
  }
}
