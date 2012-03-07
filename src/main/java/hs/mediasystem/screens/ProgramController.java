package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SublightSubtitleProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.optiondialog.DialogScreen;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class ProgramController {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);
  private static final KeyCombination KEY_S = new KeyCodeCombination(KeyCode.S);
  private static final KeyCombination KEY_CTRL_ALT_S = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);
  private static final KeyCombination KEY_OPEN_BRACKET = new KeyCodeCombination(KeyCode.OPEN_BRACKET);
  private static final KeyCombination KEY_CLOSE_BRACKET = new KeyCodeCombination(KeyCode.CLOSE_BRACKET);

  private final Stage mainStage;  // WORKAROUND: Two stages because a transparent mainstage performs so poorly; only using a transparent stage when media is playing; refactor this
  private final Stage transparentStage;
  private final Scene scene = new Scene(new BorderPane(), Color.BLACK);
  private final StackPane sceneRoot = new StackPane();
  private final BorderPane contentBorderPane = new BorderPane();
  private final BorderPane overlayBorderPane = new BorderPane();
  private final Ini ini;
  private final Provider<MainScreen> mainScreenProvider;
  private final Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider;
  private final SubtitleDownloadService subtitleDownloadService = new SubtitleDownloadService();
  private final PlayerPresentation playerPresentation;

  private final VBox messagePane = new VBox() {{
    getStylesheets().add("status-messages.css");
    getStyleClass().add("status-messages");
    setVisible(false);
  }};

  private final Navigator navigator = new Navigator();
  private final InformationBorder informationBorder = new InformationBorder();

  private int screenNumber;

  @Inject
  public ProgramController(Ini ini, final PlayerPresentation playerPresentation, Provider<MainScreen> mainScreenProvider, Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider) {
    this.ini = ini;
    this.playerPresentation = playerPresentation;
    this.mainScreenProvider = mainScreenProvider;
    this.playbackOverlayPresentationProvider = playbackOverlayPresentationProvider;

    screenNumber = Integer.parseInt(ini.getSection("general").getDefault("screen", "0"));

    sceneRoot.getChildren().addAll(contentBorderPane, overlayBorderPane);
    scene.setRoot(sceneRoot);
    scene.getStylesheets().add("default.css");

    overlayBorderPane.setMouseTransparent(true);
    overlayBorderPane.setCenter(new BorderPane() {{
      setTop(informationBorder);
    }});
    overlayBorderPane.setRight(new Group(messagePane));

    mainStage = new Stage(StageStyle.UNDECORATED);
    transparentStage = new Stage(StageStyle.TRANSPARENT);

    mainStage.setTitle("MediaSystem");
    transparentStage.setTitle("MediaSystem");

    navigator.onNavigation().set(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        List<Destination> trail = navigator.getTrail();
        StringBuilder builder = new StringBuilder();

        for(Destination dest : trail) {
          if(builder.length() > 0) {
            builder.append(" > ");
          }
          builder.append(dest.getDescription());
        }

        informationBorder.breadCrumbProperty().set(builder.toString());
      }
    });

    sceneRoot.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          navigator.back();
          event.consume();
        }
        else if(KEY_CTRL_ALT_S.match(event)) {
          ObservableList<Screen> screens = Screen.getScreens();
          screenNumber++;

          if(screenNumber >= screens.size()) {
            screenNumber = 0;
          }

          playerPresentation.setScreen(screenNumber);
          setupStageLocation(mainStage);
          setupStageLocation(transparentStage);
        }
        else if(getActiveScreen().getClass() == PlaybackOverlayPane.class) {
          KeyCode code = event.getCode();

          if(KEY_S.match(event)) {
            stop();
            event.consume();
          }
          else if(code == KeyCode.SPACE) {
            playerPresentation.pause();
            event.consume();
          }
          else if(code == KeyCode.NUMPAD4) {
            playerPresentation.move(-10 * 1000);
            event.consume();
          }
          else if(code == KeyCode.NUMPAD6) {
            playerPresentation.move(10 * 1000);
            event.consume();
          }
          else if(code == KeyCode.NUMPAD2) {
            playerPresentation.move(-60 * 1000);
            event.consume();
          }
          else if(code == KeyCode.NUMPAD8) {
            playerPresentation.move(60 * 1000);
            event.consume();
          }
          else if(code == KeyCode.M) {
            playerPresentation.mute();
            event.consume();
          }
          else if(code == KeyCode.DIGIT9) {
            playerPresentation.changeVolume(-5);
            event.consume();
          }
          else if(code == KeyCode.DIGIT0) {
            playerPresentation.changeVolume(5);
            event.consume();
          }
          else if(code == KeyCode.DIGIT1) {
            playerPresentation.changeBrightness(-0.05f);
            event.consume();
          }
          else if(code == KeyCode.DIGIT2) {
            playerPresentation.changeBrightness(0.05f);
            event.consume();
          }
          else if(code == KeyCode.Z) {
            playerPresentation.changeSubtitleDelay(-100);
            event.consume();
          }
          else if(code == KeyCode.X) {
            playerPresentation.changeSubtitleDelay(100);
            event.consume();
          }
          else if(KEY_OPEN_BRACKET.match(event)) {
            playerPresentation.changeRate(-0.1f);
            event.consume();
          }
          else if(KEY_CLOSE_BRACKET.match(event)) {
            playerPresentation.changeRate(0.1f);
            event.consume();
          }
        }
      }
    });

    messagePane.getChildren().addListener(new ListChangeListener<Node>() {
      @Override
      public void onChanged(ListChangeListener.Change<? extends Node> change) {
        messagePane.setVisible(!change.getList().isEmpty());
      }
    });

    registerWorker(subtitleDownloadService);

    subtitleDownloadService.stateProperty().addListener(new ChangeListener<State>() {
      @Override
      public void changed(ObservableValue<? extends State> observableValue, State oldValue, State newValue) {
        if(newValue == State.SUCCEEDED) {
          playerPresentation.showSubtitle(subtitleDownloadService.getValue());
        }
      }
    });
  }

  public Ini getIni() {
    return ini;
  }

  public Navigator getNavigator() {
    return navigator;
  }

  private void setupStageLocation(Stage stage) {
    ObservableList<Screen> screens = Screen.getScreens();
    Screen screen = screens.size() <= screenNumber ? Screen.getPrimary() : screens.get(screenNumber);

    Rectangle2D bounds = screen.getBounds();
    boolean primary = screen.equals(Screen.getPrimary());    // WORKAROUND: this doesn't work nice in combination with full screen, so this hack is used to prevent going fullscreen when screen is not primary

    if(primary) {
      stage.setX(bounds.getMinX());
      stage.setY(bounds.getMinY());
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());
      stage.setFullScreen(true);
    }
    else {
      stage.setX(bounds.getMinX());
      stage.setY(bounds.getMinY());
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());
      stage.toFront();
    }
  }

  private void displayOnMainStage(Node node) {
    displayOnStage(node, mainStage, transparentStage, Color.BLACK);
  }

  private void displayOnOverlayStage(Node node) {
    displayOnStage(node, transparentStage, mainStage, Color.TRANSPARENT);
  }

  private void displayOnStage(final Node node, Stage newStage, Stage oldStage, Color background) {
    oldStage.setScene(null);
    scene.setFill(background);
    contentBorderPane.setCenter(node);
    newStage.setScene(scene);
    newStage.show();

    setupStageLocation(newStage);

    newStage.toFront();
    oldStage.hide();

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        node.requestFocus();
      }
    });
  }

  public Node getActiveScreen() {
    return contentBorderPane.getCenter();
  }

  public void showMainScreen() {
    navigator.navigateTo(new Destination("Home") {
      @Override
      public void execute() {
        displayOnMainStage(mainScreenProvider.get());
      }
    });
  }

  public void showScreen(final Node node) {
    displayOnMainStage(node);
  }

  private MediaItem currentMediaItem;

  public MediaItem getCurrentMediaItem() {
    return currentMediaItem;
  }

  public void play(MediaItem mediaItem) {
    playerPresentation.getPlayer().positionProperty().set(0);
    playerPresentation.play(mediaItem.getUri());

    currentMediaItem = mediaItem;

    final PlaybackOverlayPresentation playbackOverlayPresentation = playbackOverlayPresentationProvider.get();

    navigator.navigateTo(new Destination(mediaItem.getTitle()) {
      @Override
      public void execute() {
        displayOnOverlayStage(playbackOverlayPresentation.getView());
      }
    });

    informationBorder.setVisible(false);
  }

  public void stop() {
    subtitleDownloadService.cancel();
    playerPresentation.stop();

    while(getActiveScreen().getClass() == PlaybackOverlayPane.class) {
      navigator.back();
    }

    informationBorder.setVisible(true);
  }

  public List<SubtitleProvider> getSubtitleProviders() {
    final Section general = ini.getSection("general");

    return new ArrayList<SubtitleProvider>() {{
      add(new SublightSubtitleProvider(general.get("sublight.client"), general.get("sublight.key")));
    }};
  }

  public SubtitleDownloadService getSubtitleDownloadService() {
    return subtitleDownloadService;
  }

  public void registerWorker(final Worker<?> worker) {
    final Node node = createMessage(worker);

    System.out.println("[FINE] ProgramController.registerService() - registering new service: " + worker);

    worker.stateProperty().addListener(new ChangeListener<State>() {
      @Override
      public void changed(ObservableValue<? extends State> observableValue, State oldValue, State newValue) {
        if(newValue == State.SCHEDULED) {
          messagePane.getChildren().add(node);
        }
        else if(newValue == State.SUCCEEDED || newValue == State.FAILED || newValue == State.CANCELLED) {
          messagePane.getChildren().remove(node);
        }
      }
    });
  }

  public void showOptionScreen(final String title, final List<? extends Option> options) {
    navigator.navigateToModal(new Destination(title) {
      private DialogScreen dialogScreen;

      @Override
      public void execute() {
        dialogScreen = new DialogScreen(title, options);

        sceneRoot.getChildren().add(dialogScreen);

        dialogScreen.requestFocus();
      }

      @Override
      public void outro() {
        sceneRoot.getChildren().remove(dialogScreen);
      }
    });
  }

  private static Node createMessage(final Worker<?> worker) {
    return new StackPane() {{
      getChildren().add(new VBox() {{
        setFillWidth(false);
        getStyleClass().add("item");
        getChildren().add(new HBox() {{
          setAlignment(Pos.CENTER_LEFT);
          getChildren().add(new ProgressIndicator() {{
            progressProperty().bind(worker.progressProperty());
          }});
          getChildren().add(new Label() {{
            getStyleClass().add("title");
            textProperty().bind(worker.titleProperty());
          }});
        }});
        getChildren().add(new Label() {{
          setMaxWidth(400);
          getStyleClass().add("description");
          textProperty().bind(worker.messageProperty());
        }});
      }});
    }};
  }
}
