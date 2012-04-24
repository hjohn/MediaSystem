package hs.mediasystem.screens;

import hs.mediasystem.db.DatabaseException;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.OpenSubtitlesSubtitleProvider;
import hs.mediasystem.framework.SublightSubtitleProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.optiondialog.DialogScreen;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.util.KeyCombinationGroup;
import hs.mediasystem.util.SceneManager;
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
  private static final KeyCombination FUNC_JUMP_FORWARD_SMALL = new KeyCombinationGroup(new KeyCodeCombination(KeyCode.RIGHT), new KeyCodeCombination(KeyCode.NUMPAD6));
  private static final KeyCombination FUNC_JUMP_BACKWARD_SMALL = new KeyCombinationGroup(new KeyCodeCombination(KeyCode.LEFT), new KeyCodeCombination(KeyCode.NUMPAD4));
  private static final KeyCombination FUNC_JUMP_FORWARD = new KeyCombinationGroup(new KeyCodeCombination(KeyCode.UP), new KeyCodeCombination(KeyCode.NUMPAD8));
  private static final KeyCombination FUNC_JUMP_BACKWARD = new KeyCombinationGroup(new KeyCodeCombination(KeyCode.DOWN), new KeyCodeCombination(KeyCode.NUMPAD2));

  private final Scene scene = new Scene(new BorderPane(), Color.BLACK);
  private final StackPane sceneRoot = new StackPane();
  private final BorderPane contentBorderPane = new BorderPane();
  private final BorderPane informationBorderPane = new BorderPane();
  private final BorderPane messageBorderPane = new BorderPane();
  private final Ini ini;
  private final Provider<MainScreen> mainScreenProvider;
  private final Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider;
  private final SubtitleDownloadService subtitleDownloadService = new SubtitleDownloadService();
  private final SceneManager sceneManager;
  private final PlayerPresentation playerPresentation;
  private final ItemsDao itemsDao;

  private final VBox messagePane = new VBox() {{
    getStylesheets().add("status-messages.css");
    getStyleClass().add("status-messages");
    setVisible(false);
  }};

  private final Navigator navigator = new Navigator();
  private final InformationBorder informationBorder = new InformationBorder();

  @SuppressWarnings("deprecation")
  @Inject
  public ProgramController(Ini ini, final SceneManager sceneManager, final PlayerPresentation playerPresentation, Provider<MainScreen> mainScreenProvider, Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider, ItemsDao itemsDao) {
    this.ini = ini;
    this.sceneManager = sceneManager;
    this.playerPresentation = playerPresentation;
    this.mainScreenProvider = mainScreenProvider;
    this.playbackOverlayPresentationProvider = playbackOverlayPresentationProvider;
    this.itemsDao = itemsDao;

    sceneRoot.getChildren().addAll(contentBorderPane, informationBorderPane, messageBorderPane);
    scene.setRoot(sceneRoot);
    scene.getStylesheets().add("default.css");
    scene.impl_focusOwnerProperty().addListener(new ChangeListener<Node>() {  // WORKAROUND for lack of Focus information when Stage is not focused
      @Override
      public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
        if(oldValue != null) {
          oldValue.getStyleClass().remove("focused");
          oldValue.fireEvent(new FocusEvent(false));
        }
        if(newValue != null) {
          newValue.getStyleClass().add("focused");
          newValue.fireEvent(new FocusEvent(true));
        }
      }
    });

    sceneManager.setScene(scene);

    informationBorderPane.setMouseTransparent(true);
    informationBorderPane.setTop(informationBorder);
    messageBorderPane.setMouseTransparent(true);
    messageBorderPane.setRight(new Group(messagePane));

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
          int screenNumber = sceneManager.getScreenNumber();
          screenNumber++;

          if(screenNumber >= screens.size()) {
            screenNumber = 0;
          }

          sceneManager.setScreenNumber(screenNumber);
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
          else if(FUNC_JUMP_BACKWARD_SMALL.match(event)) {
            playerPresentation.move(-10 * 1000);
            event.consume();
          }
          else if(FUNC_JUMP_FORWARD_SMALL.match(event)) {
            playerPresentation.move(10 * 1000);
            event.consume();
          }
          else if(FUNC_JUMP_BACKWARD.match(event)) {
            playerPresentation.move(-60 * 1000);
            event.consume();
          }
          else if(FUNC_JUMP_FORWARD.match(event)) {
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

  private void displayOnMainStage(Node node) {
    displayOnStage(node, Color.BLACK);
  }

  private void displayOnOverlayStage(Node node) {
    displayOnStage(node, Color.TRANSPARENT);
  }

  private void displayOnStage(final Node node, Color background) {
    contentBorderPane.setCenter(node);

    scene.setFill(background);

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
      private MainScreen mainScreen;

      @Override
      protected void init() {
        mainScreen = mainScreenProvider.get();
      }

      @Override
      public void execute() {
        displayOnMainStage(mainScreen);
      }
    });
  }

  public void showScreen(final Node node) {
    assert node != null;

    displayOnMainStage(node);
  }

  private MediaItem currentMediaItem;

  public MediaItem getCurrentMediaItem() {
    return currentMediaItem;
  }

  public void play(final MediaItem mediaItem) {
    sceneManager.setPlayerRoot(playerPresentation.getPlayer().getDisplayComponent());
    playerPresentation.getPlayer().positionProperty().set(0);
    playerPresentation.play(mediaItem.getUri());

    currentMediaItem = mediaItem;

    final PlaybackOverlayPresentation playbackOverlayPresentation = playbackOverlayPresentationProvider.get();

    navigator.navigateTo(new Destination(mediaItem.getTitle()) {
      private long totalTimeViewed = 0;

      @Override
      protected void init() {
        playerPresentation.getPlayer().positionProperty().addListener(new ChangeListener<Number>() {
          @Override
          public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number value) {
            long old = oldValue.longValue();
            long current = value.longValue();

            if(old < current) {
              long diff = current - old;

              if(diff < 1000) {
                totalTimeViewed += diff;
              }
            }
          }
        });
      }

      @Override
      public void execute() {
        displayOnOverlayStage(playbackOverlayPresentation.getView());
      }

      @Override
      protected void outro() {
        if(totalTimeViewed > playerPresentation.getLength() * 4 / 5) {  // more than 80% viewed?
          try {
            System.out.println("[CONFIG] ProgramController.play(...).new Destination() {...}.outro() - Marking as viewed: " + mediaItem);

            mediaItem.viewedProperty().set(true);
            itemsDao.changeItemViewedStatus(mediaItem.getDatabaseId(), true);
          }
          catch(DatabaseException e) {
            System.out.println("[WARNING] ProgramController.play(...).new Destination() {...}.outro() - Unable to update viewed status for " + mediaItem + ": " + e);
          }
        }
      }
    });

//    informationBorder.setVisible(false);
  }

  public void stop() {
    while(getActiveScreen().getClass() == PlaybackOverlayPane.class) {
      navigator.back();
    }

    informationBorder.setVisible(true);

    playerPresentation.stop();
    sceneManager.disposePlayerRoot();

    subtitleDownloadService.cancel();
  }

  public List<SubtitleProvider> getSubtitleProviders() {
    final Section general = ini.getSection("general");

    return new ArrayList<SubtitleProvider>() {{
      add(new SublightSubtitleProvider(general.get("sublight.client"), general.get("sublight.key")));
      add(new OpenSubtitlesSubtitleProvider("MediaSystem v1"));
    }};
  }

  public SubtitleDownloadService getSubtitleDownloadService() {
    return subtitleDownloadService;
  }

  public final void registerWorker(final Worker<?> worker) {
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
