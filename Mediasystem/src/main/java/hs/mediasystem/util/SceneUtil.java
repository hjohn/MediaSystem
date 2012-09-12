package hs.mediasystem.util;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneUtil {
  public static Scene createScene(Parent root) {
    Scene scene = new Scene(root);

    final EventDispatcher eventDispatcher = scene.getEventDispatcher();

    scene.setEventDispatcher(new EventDispatcher() {
      @Override
      public Event dispatchEvent(Event event, EventDispatchChain tail) {
        long millis = System.currentTimeMillis();

        Event returnedEvent = eventDispatcher.dispatchEvent(event, tail);

        millis = System.currentTimeMillis() - millis;

        if(millis >= 100) {
          System.out.println("[WARN] Slow Event Handling: " + millis + " ms for event: " + event);
        }

        return returnedEvent;
      }
    });

    scene.getStylesheets().add("default.css");
    scene.focusOwnerProperty().addListener(new ChangeListener<Node>() {  // WORKAROUND for lack of Focus information when Stage is not focused
      @Override
      public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
        if(oldValue != null) {
          oldValue.getStyleClass().remove("focused");
          oldValue.fireEvent(new FocusEvent(false));
        }
        if(newValue != null) {
          newValue.getStyleClass().add("focused");
          newValue.fireEvent(new FocusEvent(true));
        }
      }
    });

    return scene;
  }
}
