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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import javax.inject.Inject;

public class MainScreen {
  private final ProgramController controller;
  private final Set<MainMenuExtension> mainMenuExtensions;

  @Inject
  public MainScreen(ProgramController controller, Set<MainMenuExtension> mainMenuExtensions) {
    this.controller = controller;
    this.mainMenuExtensions = mainMenuExtensions;
  }

  public Node create() {
    final BorderPane root = new BorderPane();

    final List<Button> buttons = new ArrayList<>();

    for(final MainMenuExtension mainMenuExtension : mainMenuExtensions) {
      buttons.add(new Button(mainMenuExtension.getTitle()) {{
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


    final VBox menuBox = new VBox() {{
      getChildren().addAll(buttons);
    }};

    HBox box = new HBox() {{
      getChildren().add(menuBox);
    }};

    ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        Timeline timeline = new Timeline();
        Button b = (Button)(((ReadOnlyProperty<? extends Boolean>)observable).getBean());

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
    }

    root.setCenter(box);

    return root;
  }
}
