package hs.mediasystem.controls;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class ControlTest2 extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    HBox box = new HBox();



    box.getChildren().add(new Button("Button"));
    box.getChildren().add(new Button("Button with Clip") {{
      setClip(new Rectangle(0, 0, 200, 200));
    }});
    box.getChildren().add(new Button("Half Opaque Button") {{
      setOpacity(0.5);
    }});
    box.getChildren().add(new Label() {{
      setGraphic(new Button("Half Opaque Button with Clip") {{
        setStyle("-fx-font-size: 50px");
        setOpacity(0.5);
        setEffect(new PerspectiveTransform(100, 80, 200, 100, 200, 140, 100, 160) {{
          setInput(new Reflection());
        }});
        setClip(new Rectangle(0, 0, 150, 200));
      }});
    }});

    Scene scene = new Scene(box);

    stage.setScene(scene);
    stage.show();
  }
}
