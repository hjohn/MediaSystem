package hs.mediasystem.screens;

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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.BoxBlur;
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

  private final StackPane menuBox;

  private Button lastSelected;

  @Inject
  public MainScreen(final ProgramController controller, final PluginTracker<MainMenuExtension> tracker) {
    getStylesheets().add("main-screen.css");

    menuBox = new StackPane();

    tracker.getPlugins().addListener(new ListChangeListener<MainMenuExtension>() {
      @Override
      public void onChanged(ListChangeListener.Change<? extends MainMenuExtension> event) {
        for(Timeline timeline : timelines) {
          timeline.stop();
        }
        for(Timeline timeline : timelines2) {
          timeline.stop();
        }

        timelines.clear();
        timelines2.clear();
        stackPanes.clear();
        buttons.clear();

        final Set<MainMenuExtension> extensions = new TreeSet<>(new Comparator<MainMenuExtension>() {
          @Override
          public int compare(MainMenuExtension o1, MainMenuExtension o2) {
            return Double.compare(o1.order(), o2.order());
          }
        });

        extensions.addAll(tracker.getPlugins());

        for(final MainMenuExtension mainMenuExtension : extensions) {
          final Button b = new Button(mainMenuExtension.getTitle()) {{
            setGraphic(new ImageView(mainMenuExtension.getImage()));
            setOnAction(new EventHandler<ActionEvent>() {
              @Override
              public void handle(ActionEvent event) {
                mainMenuExtension.select(controller);
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

        for(int i = 0; i < buttons.size(); i++) {
          final Button button = buttons.get(i);
          final StackPane sp = stackPanes.get(i);
          final int buttonIndex = i;

          button.addEventHandler(FocusEvent.ANY, new EventHandler<FocusEvent>() {
            @Override
            public void handle(FocusEvent event) {
              boolean focusGained = event.getEventType().equals(FocusEvent.FOCUS_GAINED);

              handleButtonFocus(focusGained, buttonIndex);

              if(focusGained) {
                lastSelected = button;
              }
            }
          });
          sp.minWidthProperty().bind(button.widthProperty());
        }

        menuBox.getChildren().clear();

        menuBox.getChildren().add(new HBox() {{
          getStyleClass().add("menu-scroll-box");
          getChildren().addAll(stackPanes);
        }});
        menuBox.getChildren().add(new HBox() {{
          getStyleClass().add("menu-scroll-box");
          getChildren().addAll(buttons);
        }});
      }
    });

    StackPane stackPane = new StackPane();

    final HBox menuBoxOverlay = new HBox() {{
      getStyleClass().add("menu-scroll-box-overlay");
    }};

    stackPane.getChildren().addAll(menuBox, menuBoxOverlay);

    setCenter(stackPane);
  }

  @Override
  public void requestFocus() {
    if(lastSelected != null) {
      lastSelected.requestFocus();
    }
    else {
      if(!buttons.isEmpty()) {
        buttons.get(0).requestFocus();
      }
    }
  }

  private void handleButtonFocus(final boolean focused, final int buttonIndex) {
    if(buttonIndex >= buttons.size()) {
      return;
    }

    Button b = buttons.get(buttonIndex);

    if(focused) {
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
        new KeyValue(b.scaleXProperty(), focused ? 1.2 : 1.0),
        new KeyValue(b.scaleYProperty(), focused ? 1.2 : 1.0)
      ),
      new KeyFrame(new Duration(5000), "x",
        new KeyValue(sp.scaleXProperty(), focused ? 2.0 : 1.0, Interpolator.EASE_BOTH),
        new KeyValue(sp.scaleYProperty(), focused ? 1.5 : 1.0, Interpolator.EASE_BOTH)
      ),
      new KeyFrame(new Duration(10000),
        new KeyValue(sp.scaleXProperty(), focused ? 1.8 : 1.0, Interpolator.EASE_BOTH),
        new KeyValue(sp.scaleYProperty(), focused ? 1.7 : 1.0, Interpolator.EASE_BOTH)
      ),
      new KeyFrame(new Duration(15000),
        new KeyValue(sp.scaleXProperty(), focused ? 2.2 : 1.0, Interpolator.EASE_BOTH),
        new KeyValue(sp.scaleYProperty(), focused ? 1.3 : 1.0, Interpolator.EASE_BOTH)
      ),
      new KeyFrame(new Duration(20000), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          if(focused) {
            timeline.playFrom("x");
          }
        }
      },
        new KeyValue(sp.scaleXProperty(), focused ? 2.0 : 1.0, Interpolator.EASE_BOTH),
        new KeyValue(sp.scaleYProperty(), focused ? 1.5 : 1.0, Interpolator.EASE_BOTH)
      )
    );

    timeline.play();

    final Timeline timeline2 = timelines2.get(buttonIndex);

    timeline2.stop();

    if(focused) {
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
