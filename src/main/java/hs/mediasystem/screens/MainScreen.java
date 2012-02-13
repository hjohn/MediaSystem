package hs.mediasystem.screens;

import hs.mediasystem.screens.Navigator.Destination;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import javax.inject.Inject;

public class MainScreen extends BorderPane {
  private final List<Button> buttons = new ArrayList<>();

  @Inject
  public MainScreen(final ProgramController controller, final Set<MainMenuExtension> mainMenuExtensions) {
    setId("main-screen");

    for(final MainMenuExtension mainMenuExtension : mainMenuExtensions) {
      buttons.add(new Button(mainMenuExtension.getTitle()) {{
        getStyleClass().add("option");
        setGraphic(new ImageView(mainMenuExtension.getImage()));
        setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            controller.getNavigator().navigateTo(new Destination(mainMenuExtension.getTitle()) {
              private Node view;

              @Override
              public void init() {
                view = mainMenuExtension.select(controller);
              }

              @Override
              public void execute() {
                controller.showScreen(view);
              }
            });
          }
        });
      }});
    }


    final HBox menuBox = new HBox() {{
      setAlignment(Pos.CENTER);
      getChildren().addAll(buttons);
    }};

    ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        Timeline timeline = new Timeline();
        Button b = (Button)(((ReadOnlyProperty<? extends Boolean>)observable).getBean());

        double d = b.getParent().getLayoutBounds().getWidth() - b.getLayoutBounds().getWidth();
        timeline.getKeyFrames().addAll(
          // new KeyFrame(Duration.ZERO, new KeyValue(menuBox.translateYProperty(), menuBox.getTranslateY())),
          new KeyFrame(new Duration(500), new KeyValue(menuBox.translateXProperty(), -b.getLayoutX() + d / 2))
        );

        timeline.play();
      }
    };

    for(final Button button : buttons) {
      button.focusedProperty().addListener(changeListener);
    }

    setCenter(menuBox);
  }

  @Override
  public void requestFocus() {
    buttons.get(0).requestFocus();
  }
}
