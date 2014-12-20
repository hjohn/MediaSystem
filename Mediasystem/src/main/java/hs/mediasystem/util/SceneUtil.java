package hs.mediasystem.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.sun.javafx.tk.Toolkit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneUtil {
  private static final ScheduledExecutorService EVENT_TIMEOUT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

  public static Scene createScene(Parent root) {
    Scene scene = new Scene(root);

    final EventDispatcher eventDispatcher = scene.getEventDispatcher();

    scene.setEventDispatcher(new EventDispatcher() {
      @Override
      public Event dispatchEvent(Event event, EventDispatchChain tail) {
        Thread fxThread = Thread.currentThread();

        ScheduledFuture<?> future = EVENT_TIMEOUT_EXECUTOR.schedule(new Runnable() {
          @Override
          public void run() {
            if(!Toolkit.getToolkit().isNestedLoopRunning()) {
              System.out.println("[WARN] Slow Event Handling, trace:");

              for(StackTraceElement element : fxThread.getStackTrace()) {
                System.out.println("[WARN]   -- " + element);
              }
            }
          }
        }, 1000, TimeUnit.MILLISECONDS);

        Event returnedEvent = eventDispatcher.dispatchEvent(event, tail);

        future.cancel(false);

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

          System.out.println("[INFO] Focus set to: " + newValue);
        }
      }
    });

    return scene;
  }
}
