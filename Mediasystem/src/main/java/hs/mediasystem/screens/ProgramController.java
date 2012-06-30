package hs.mediasystem.screens;

import hs.mediasystem.dao.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.player.PlayerEvent;
import hs.mediasystem.framework.player.PlayerEvent.Type;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.optiondialog.DialogScreen;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.util.KeyCombinationGroup;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.SceneUtil;
import hs.mediasystem.util.annotation.Nullable;
import hs.mediasystem.util.ini.Ini;

import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

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

  private final Scene scene;
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

  private final VBox messagePane = new VBox() {{
    getStylesheets().add("status-messages.css");
    getStyleClass().add("status-messages");
    setVisible(false);
  }};

  private final Navigator navigator = new Navigator();
  private final InformationBorder informationBorder;

  @Inject
  public ProgramController(Ini ini, final SceneManager sceneManager, @Nullable final PlayerPresentation playerPresentation, Provider<MainScreen> mainScreenProvider, Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider, InformationBorder informationBorder) {
    this.ini = ini;
    this.sceneManager = sceneManager;
    this.playerPresentation = playerPresentation;
    this.mainScreenProvider = mainScreenProvider;
    this.playbackOverlayPresentationProvider = playbackOverlayPresentationProvider;
    this.informationBorder = informationBorder;
    this.scene = SceneUtil.createScene(sceneRoot);

    sceneRoot.getChildren().addAll(contentBorderPane, informationBorderPane, messageBorderPane);

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

        ProgramController.this.informationBorder.breadCrumbProperty().set(builder.toString());
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
          if(playerPresentation != null) {
            if(code == KeyCode.SPACE) {
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
        if(newValue == State.SUCCEEDED && playerPresentation != null) {
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
    Timeline timeline = new Timeline(
      new KeyFrame(Duration.ZERO, new KeyValue(scene.getRoot().opacityProperty(), 0.0)),
      new KeyFrame(Duration.seconds(1), new KeyValue(scene.getRoot().opacityProperty(), 1.0))
    );

    timeline.play();

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
    navigator.navigateTo(new Destination("home", "Home") {
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

  public synchronized void play(final MediaItem mediaItem) {
    if(playerPresentation == null) {
      sceneManager.displayDialog(new InformationDialog("No video player was configured.\nUnable to play the selected item."));
      return;
    }

    int resumePosition = 0;

    if(mediaItem.isCachable()) {
      final ResumeDialog resumeDialog = new ResumeDialog(mediaItem);

      sceneManager.displaySynchronousDialog(resumeDialog);

      if(resumeDialog.wasCancelled()) {
        return;
      }

      resumePosition = resumeDialog.getResumePosition();
    }

    final int finalResumePosition = resumePosition;

    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), new KeyValue(scene.getRoot().opacityProperty(), 0.0)));

    timeline.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        startPlay(mediaItem, finalResumePosition * 1000L);
      }
    });
    timeline.play();
  }

  private void startPlay(final MediaItem mediaItem, long positionMillis) {
    sceneManager.setPlayerRoot(playerPresentation.getPlayer().getDisplayComponent());
    playerPresentation.getPlayer().onPlayerEvent().set(new EventHandler<PlayerEvent>() {
      @Override
      public void handle(PlayerEvent event) {
        if(event.getType() == Type.FINISHED) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              stop();
            }
          });
          event.consume();
        }
      }
    });
    playerPresentation.getPlayer().positionProperty().set(0);
    playerPresentation.play(mediaItem.getUri(), positionMillis);
    currentMediaItem = mediaItem;

    final PlaybackOverlayPresentation playbackOverlayPresentation = playbackOverlayPresentationProvider.get();

    navigator.navigateTo(new Destination("playback", mediaItem.getTitle()) {
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
        MediaData mediaData = mediaItem.get(MediaData.class);

        if(mediaData != null) {
          long timeViewed = totalTimeViewed + mediaData.getResumePosition() * 1000L;

          if(timeViewed >= playerPresentation.getLength() * 9 / 10) {  // 90% viewed?
            System.out.println("[CONFIG] ProgramController.play(...).new Destination() {...}.outro() - Marking as viewed: " + mediaItem);

            mediaData.viewedProperty().set(true);
          }

          if(totalTimeViewed > 30 * 1000) {
            long resumePosition = 0;
            long position = playerPresentation.getPosition();

            if(position > 30 * 1000 && position < playerPresentation.getLength() * 9 / 10) {
              System.out.println("[CONFIG] ProgramController.play(...).new Destination() {...}.outro() - Setting resume position to " + position + " ms: " + mediaItem);

              resumePosition = position;
            }

            mediaData.resumePositionProperty().set((int)(resumePosition / 1000));
          }
        }
      }
    });

    informationBorder.setVisible(false);
  }

  public synchronized void stop() {
    playerPresentation.getPlayer().onPlayerEvent().set(null);

    while(getActiveScreen().getClass() == PlaybackOverlayPane.class) {
      navigator.back();
    }

    informationBorder.setVisible(true);

    playerPresentation.stop();
    sceneManager.disposePlayerRoot();

    subtitleDownloadService.cancel();
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

  public void showDialog(final String title, final Node dialog) {
    navigator.navigateToModal(new Destination("optionDialog", title) {

      @Override
      public void execute() {
        sceneRoot.getChildren().add(dialog);

        dialog.requestFocus();
      }

      @Override
      public void outro() {
        sceneRoot.getChildren().remove(dialog);
      }
    });
  }

  public void showOptionScreen(final String title, final List<? extends Option> options) {
    showDialog(title, new DialogScreen(title, options));
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
