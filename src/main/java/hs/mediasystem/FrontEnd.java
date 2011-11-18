package hs.mediasystem;

import hs.mediasystem.framework.Player;
import hs.mediasystem.fs.MoviesMediaTree;
import hs.mediasystem.players.vlc.VLCPlayer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class FrontEnd extends Application {
  private Player player;
  
  @Override
  public void start(Stage primaryStage) throws Exception {
    player = new VLCPlayer();  // TODO make configurable
    
    final BorderPane root = new BorderPane();
    Scene scene = new Scene(root, new Color(0, 0, 0, 0.01));
    
    final Stage stage = new Stage(StageStyle.TRANSPARENT);
    
    stage.setScene(scene);
    //stage.setFullScreen(true);
    
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();

    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());
    stage.setWidth(bounds.getWidth());
    stage.setHeight(bounds.getHeight());
    
    scene.getStylesheets().add("default.css");

    final List<Button> buttons = new ArrayList<Button>() {{
      add(new Button("Movies") {{
        setGraphic(new ImageView(new Image("images/package_multimedia.png")));
      }});
      add(new Button("Series") {{
        setGraphic(new ImageView(new Image("images/aktion.png")));
      }});
      add(new Button("Nederland 24") {{
        setGraphic(new ImageView(new Image("images/browser.png")));
      }});
      add(new Button("Youtube") {{
        setGraphic(new ImageView(new Image("images/tv.png")));
      }});
    }};
    
    
    final VBox menuBox = new VBox() {{
      getChildren().addAll(buttons);
    }};
    
    ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        Timeline timeline = new Timeline();
        Button b = (Button)(((ReadOnlyProperty<? extends Boolean>)observable).getBean());
        
        System.out.println("focus changed");
        
        Point2D point = b.localToParent(0, b.getLayoutY());
        
        System.out.println("point = " + point + " ; " + b.getLayoutY());
        
        double d = b.getParent().getLayoutBounds().getHeight();
        timeline.getKeyFrames().addAll(
          new KeyFrame(Duration.ZERO, new KeyValue(menuBox.translateYProperty(), menuBox.getTranslateY())),
          new KeyFrame(new Duration(500), new KeyValue(menuBox.translateYProperty(), -b.getLayoutY() + d / 2))
        );
        
        timeline.play();
      }
    };
    
    for(final Button button : buttons) {
      button.focusedProperty().addListener(changeListener);
      button.addEventHandler(EventType.ROOT, new EventHandler<Event>() {
        @Override
        public void handle(Event event) {
          System.out.println("Button " + button.getText() + " received " + event);
        }
      });
    }
    
    buttons.get(0).setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        SelectItemScene scene = new SelectItemScene();
        
        Node node = scene.show(stage, new MoviesMediaTree(Paths.get("d:/Dev/Library/movies")));

        root.setCenter(node);
        
        //stage.widthProperty().divide(2);
      }
    });
    
    root.setCenter(menuBox);
    
    root.setTop(new HBox() {{
      setId("top-bar");
      getChildren().add(new Label("MediaSystem"));
    }});
    
    stage.show();
    //primaryStage.show();
  }
}
