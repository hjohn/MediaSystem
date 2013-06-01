package hs.mediasystem.controls;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PTTest extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {

    Label label = new Label();
    label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    label.setGraphic(new ImageView("http://myblog.arts.ac.uk/wp-content/uploads/2010/11/creative_commons.jpg"));

    label.setStyle("-fx-background-color: red");

    StackPane bp = new StackPane();

    bp.getChildren().add(label);

    label.setEffect(new PerspectiveTransform(0, 0, 100, 20, 100, 680, 0, 700));

    Scene scene = new Scene(bp);

    stage.setWidth(800);
    stage.setHeight(600);
    stage.setScene(scene);
    stage.show();
  }
}
