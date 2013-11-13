package hs.mediasystem.util.javafx;

import hs.mediasystem.util.DialogPane;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * Helper class for displaying Dialogs.
 */
public class Dialogs {

  public static void show(Scene scene, DialogPane<?> dialogPane) {
    dialogPane.showDialog(scene, false);
  }

  public static void show(Event event, DialogPane<?> dialogPane) {
    dialogPane.showDialog(extractSceneFromEvent(event), false);
  }

  public static <R> R showAndWait(Scene scene, DialogPane<R> dialogPane) {
    return dialogPane.showDialog(scene, true);
  }

  public static <R> R showAndWait(Event event, DialogPane<R> dialogPane) {
    return dialogPane.showDialog(extractSceneFromEvent(event), true);
  }

  private static Scene extractSceneFromEvent(Event event) {
    return ((Node)event.getTarget()).getScene();
  }
}
