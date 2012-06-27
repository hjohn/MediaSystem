package hs.mediasystem.screens;

import hs.mediasystem.util.DialogStage;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class InformationDialog extends DialogStage {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final VBox root = new VBox();

  public InformationDialog(String text) {
    root.getStylesheets().add("default.css");
    root.getStylesheets().add("select-media/resume-dialog.css");
    root.getStyleClass().add("resume-dialog");
    root.getChildren().add(new Label(text));
    root.setFillWidth(true);

    setScene(new Scene(root));

    getScene().setFill(Color.TRANSPARENT);
    getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          close();
        }
      }
    });
  }
}
