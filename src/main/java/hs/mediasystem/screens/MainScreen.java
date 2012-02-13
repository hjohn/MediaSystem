package hs.mediasystem.screens;

import hs.mediasystem.screens.Navigator.Destination;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import javax.inject.Inject;

public class MainScreen extends BorderPane {
  private final List<Button> buttons = new ArrayList<>();
  private final List<StackPane> stackPanes = new ArrayList<>();
  private final List<Timeline> timelines = new ArrayList<>();
  private final List<Timeline> timelines2 = new ArrayList<>();
  private final Random rnd = new Random();

  @Inject
  public MainScreen(final ProgramController controller, final Set<MainMenuExtension> mainMenuExtensions) {
    setId("main-screen");

    final Set<MainMenuExtension> extensions = new TreeSet<>(new Comparator<MainMenuExtension>() {
      @Override
      public int compare(MainMenuExtension o1, MainMenuExtension o2) {
        return Double.compare(o1.order(), o2.order());
      }
    });

    extensions.addAll(mainMenuExtensions);

    extensions.add(new MainMenuExtension() {
      @Override
      public String getTitle() {
        return "Exit";
      }

      @Override
      public Image getImage() {
        return new Image("images/logout.png");
      }

      @Override
      public Node select(ProgramController controller) {
        return null;
      }

      @Override
      public double order() {
        return 1.0;
      }
    });


    extensions.add(new MainMenuExtension() {
      @Override
      public String getTitle() {
        return "Youtube";
      }

      @Override
      public Image getImage() {
        return new Image("images/musicstore.png");
      }

      @Override
      public Node select(ProgramController controller) {
        return null;
      }

      @Override
      public double order() {
        return 0.9;
      }
    });

    for(final MainMenuExtension mainMenuExtension : extensions) {
      final Button b = new Button(mainMenuExtension.getTitle()) {{
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
      }};

      timelines.add(new Timeline());
      timelines2.add(new Timeline());
      stackPanes.add(new StackPane() {{
        for(int i = 0; i < 5; i++) {
          getChildren().add(new Circle(20) {{
            double angle = 360 / extensions.size() * buttons.size();

            setFill(Color.hsb(rnd.nextDouble() * 180 + angle, 1.0, 0.45, 0.2));
            setScaleX(5.0);
            setTranslateX((rnd.nextDouble() - 0.5) * 80);
            setTranslateY((rnd.nextDouble() - 0.5) * 20 + 10);
          }});
        }
        setEffect(new BoxBlur(10, 10, 3));
      }});
      buttons.add(b);
    }

    StackPane stackPane = new StackPane();

    final HBox menuBoxOverlay = new HBox() {{
      getStyleClass().add("menu-scroll-box-overlay");
    }};

    final StackPane menuBox = new StackPane() {{
      getChildren().add(new HBox() {{
        getStyleClass().add("menu-scroll-box");
        getChildren().addAll(stackPanes);
      }});
      getChildren().add(new HBox() {{
        getStyleClass().add("menu-scroll-box");
        getChildren().addAll(buttons);
      }});
    }};

    stackPane.getChildren().addAll(menuBox, menuBoxOverlay);

    for(int i = 0; i < buttons.size(); i++) {
      final Button button = buttons.get(i);
      final StackPane sp = stackPanes.get(i);

      button.focusedProperty().addListener(new FocusListener(menuBox, i));
      sp.minWidthProperty().bind(button.widthProperty());
    }

    setCenter(stackPane);
  }

  @Override
  public void requestFocus() {
    buttons.get(0).requestFocus();
  }

  private final class FocusListener implements ChangeListener<Boolean> {
    private final StackPane menuBox;
    private final int buttonIndex;

    private FocusListener(StackPane menuBox, int button) {
      this.menuBox = menuBox;
      this.buttonIndex = button;
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, final Boolean newValue) {
      Button b = buttons.get(buttonIndex);

      if(newValue) {
        Timeline timeline = new Timeline();

        Point2D buttonCenter = b.localToScene(b.getWidth() / 2, 0);
        Point2D boxCenter = menuBox.localToScene(menuBox.getLayoutBounds().getWidth() / 2, 0);
        double distanceToCenter = boxCenter.getX() - buttonCenter.getX();

        timeline.getKeyFrames().addAll(
          new KeyFrame(new Duration(500),
            new KeyValue(menuBox.translateXProperty(), distanceToCenter)
          )
        );

        timeline.play();
      }

      final Timeline timeline = timelines.get(buttonIndex);
      final StackPane sp = stackPanes.get(buttonIndex);

      timeline.stop();
      timeline.getKeyFrames().clear();
      timeline.getKeyFrames().addAll(
        new KeyFrame(new Duration(500),
          new KeyValue(b.scaleXProperty(), newValue ? 1.2 : 1.0),
          new KeyValue(b.scaleYProperty(), newValue ? 1.2 : 1.0)
        ),
        new KeyFrame(new Duration(5000), "x",
          new KeyValue(sp.scaleXProperty(), newValue ? 2.0 : 1.0, Interpolator.EASE_BOTH),
          new KeyValue(sp.scaleYProperty(), newValue ? 1.5 : 1.0, Interpolator.EASE_BOTH)
        ),
        new KeyFrame(new Duration(10000),
          new KeyValue(sp.scaleXProperty(), newValue ? 1.8 : 1.0, Interpolator.EASE_BOTH),
          new KeyValue(sp.scaleYProperty(), newValue ? 1.7 : 1.0, Interpolator.EASE_BOTH)
        ),
        new KeyFrame(new Duration(15000),
          new KeyValue(sp.scaleXProperty(), newValue ? 2.2 : 1.0, Interpolator.EASE_BOTH),
          new KeyValue(sp.scaleYProperty(), newValue ? 1.3 : 1.0, Interpolator.EASE_BOTH)
        ),
        new KeyFrame(new Duration(20000), new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            if(newValue) {
              timeline.playFrom("x");
            }
          }
        },
          new KeyValue(sp.scaleXProperty(), newValue ? 2.0 : 1.0, Interpolator.EASE_BOTH),
          new KeyValue(sp.scaleYProperty(), newValue ? 1.5 : 1.0, Interpolator.EASE_BOTH)
        )
      );

      timeline.play();

      final Timeline timeline2 = timelines2.get(buttonIndex);

      timeline2.stop();

      if(newValue) {
        timeline2.getKeyFrames().clear();
        timeline2.getKeyFrames().add(createKeyFrame(sp, buttonIndex));
        timeline2.onFinishedProperty().set(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            timeline2.getKeyFrames().clear();
            timeline2.getKeyFrames().add(createKeyFrame(sp, buttonIndex));
            timeline2.playFromStart();
          }
        });
        timeline2.play();
      }
    }
  }

  private KeyFrame createKeyFrame(StackPane stackPane, int index) {
    List<KeyValue> keyValues = new ArrayList<>();
    ObservableList<Node> children = stackPane.getChildren();

    for(int i = 0; i < children.size(); i++) {
      Node node = children.get(i);
      double angle = 360 / stackPanes.size() * index;

      keyValues.add(new KeyValue(((Circle)node).fillProperty(), Color.hsb(rnd.nextDouble() * 180 + angle, 1.0, 0.45, 0.2), Interpolator.EASE_BOTH));
      keyValues.add(new KeyValue(node.translateXProperty(), (rnd.nextDouble() - 0.5) * 80, Interpolator.EASE_BOTH));
      keyValues.add(new KeyValue(node.translateYProperty(), (rnd.nextDouble() - 0.5) * 20 + 10, Interpolator.EASE_BOTH));
    }

    return new KeyFrame(Duration.seconds(5), null, null, keyValues);
  }
}
