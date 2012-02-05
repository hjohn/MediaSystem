package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SublightSubtitleProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
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

  private final Player player;
  private final Stage mainStage;  // TODO two stages because a transparent mainstage performs so poorly; only using a transparent stage when media is playing; refactor this
  private final Stage transparentStage;
  private final Scene scene = new Scene(new BorderPane(), Color.BLACK);
  private final StackPane sceneRoot = new StackPane();
  private final BorderPane contentBorderPane = new BorderPane();
  private final BorderPane overlayBorderPane = new BorderPane();
  private final Ini ini;
  private final Provider<MainScreen> mainScreenProvider;
  private final Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider;
  private final SubtitleDownloadService subtitleDownloadService = new SubtitleDownloadService();

  private final VBox messagePane = new VBox() {{
    setId("status-messages");
    setVisible(false);
  }};

  private final Navigator navigator = new Navigator();
  private final int screenNumber;

  @Inject
  public ProgramController(Ini ini, Player player, Provider<MainScreen> mainScreenProvider, Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider) {
    this.ini = ini;
    this.player = player;
    this.mainScreenProvider = mainScreenProvider;
    this.playbackOverlayPresentationProvider = playbackOverlayPresentationProvider;

    screenNumber = Integer.parseInt(ini.getSection("general").getDefault("screen", "0"));

    sceneRoot.getChildren().addAll(contentBorderPane, overlayBorderPane);
    scene.setRoot(sceneRoot);

    overlayBorderPane.setMouseTransparent(true);
    overlayBorderPane.setTop(new InformationBorder());
    overlayBorderPane.setRight(messagePane);

    mainStage = new Stage(StageStyle.UNDECORATED);
    transparentStage = new Stage(StageStyle.TRANSPARENT);

//    setupStage(mainStage);
//    setupStage(transparentStage);

//    mainGroup.addEventHandler(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
//      @Override
//      public void handle(KeyEvent event) {
//        System.out.println(event);
//
//      }
//    });

    contentBorderPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          navigator.back();
          event.consume();
        }
      }
    });

    messagePane.getChildren().addListener(new ListChangeListener<Node>() {
      @Override
      public void onChanged(ListChangeListener.Change<? extends Node> change) {
        messagePane.setVisible(!change.getList().isEmpty());
      }
    });

    registerService(subtitleDownloadService);

    subtitleDownloadService.stateProperty().addListener(new ChangeListener<State>() {
      @Override
      public void changed(ObservableValue<? extends State> observableValue, State oldValue, State newValue) {
        if(newValue == State.SUCCEEDED) {
          getPlayer().showSubtitle(subtitleDownloadService.getValue());
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

    Rectangle2D bounds = screen.getVisualBounds();
    boolean primary = screen.equals(Screen.getPrimary());    // TODO this doesn't work nice in combination with full screen, so this hack is used to prevent going fullscreen when screen is not primary

    if(primary) {
      stage.setFullScreen(true);
    }
    else {
      stage.setX(bounds.getMinX());
      stage.setY(bounds.getMinY());
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());
    }
  }

  private void displayOnMainStage(Node node) {
    displayOnStage(node, mainStage, transparentStage, Color.BLACK);
  }

  private void displayOnOverlayStage(Node node) {
    displayOnStage(node, transparentStage, mainStage, Color.TRANSPARENT);
  }

  private void displayOnStage(Node node, Stage newStage, Stage oldStage, Color background) {
    ObservableList<String> stylesheets = scene.getStylesheets();

    stylesheets.clear();
    stylesheets.addAll("default.css", "status-messages.css", node.getId() + ".css");

    oldStage.setScene(null);
    scene.setFill(background);
    contentBorderPane.setCenter(node);
    newStage.setScene(scene);
    newStage.show();

    setupStageLocation(newStage);

    newStage.toFront();
    oldStage.hide();
  }

  public void showMainScreen() {
    navigator.navigateTo(new Destination("Home") {
      @Override
      public void go() {
        displayOnMainStage(mainScreenProvider.get().create());
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
    player.play(mediaItem.getUri());

    currentMediaItem = mediaItem;

    final PlaybackOverlayPresentation playbackOverlayPresentation = playbackOverlayPresentationProvider.get();

    navigator.navigateTo(new Destination("Play") {
      @Override
      public void go() {
        displayOnOverlayStage(playbackOverlayPresentation.getView());
      }
    });
  }

  public void stop() {
    subtitleDownloadService.cancel();
    player.stop();

    navigator.back();
  }

  public void pause() {
    player.pause();
  }

  public void move(int ms) {
    long position = player.getPosition();
    long length = player.getLength();
    long newPosition = position + ms;

    System.out.println("Position = " + position + "; length = " + length + "; np = " + newPosition);

    if(newPosition > length - 5000) {
      newPosition = length - 5000;
    }
    if(newPosition < 0) {
      newPosition = 0;
    }

    if(Math.abs(newPosition - position) > 5000) {
      player.setPosition(newPosition);
    }
  }

  public void mute() {
    player.setMute(!player.isMute());
  }

  public void changeVolume(int volumeDiff) {
    int volume = player.getVolume() + volumeDiff;

    if(volume > 100) {
      volume = 100;
    }
    if(volume < 0) {
      volume = 0;
    }

    player.setVolume(volume);
  }

  public void changeBrightness(float brightnessDiff) {
    float brightness = player.getBrightness() + brightnessDiff;

    if(brightness > 2.0) {
      brightness = 2.0f;
    }
    else if(brightness < 0.0) {
      brightness = 0.0f;
    }

    player.setBrightness(brightness);
  }

  public void changeSubtitleDelay(int msDelayDiff) {
    int delay = player.getSubtitleDelay() + msDelayDiff;

    player.setSubtitleDelay(delay);
  }

  public int getVolume() {
    return player.getVolume();
  }

  public long getPosition() {
    return player.getPosition();
  }

  public long getLength() {
    return player.getLength();
  }

  /**
   * Returns the current subtitle.  Never returns <code>null</code> but will return a special Subtitle
   * instance for when subtitles are unavailable or disabled.
   *
   * @return the current subtitle
   */
  public Subtitle nextSubtitle() {
    List<Subtitle> subtitles = player.getSubtitles();

    Subtitle currentSubtitle = player.getSubtitle();
    int index = subtitles.indexOf(currentSubtitle) + 1;

    if(index >= subtitles.size()) {
      index = 0;
    }

    player.setSubtitle(subtitles.get(index));

    return subtitles.get(index);
  }

  public Player getPlayer() {
    return player;
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

  public void registerService(final Service<?> service) {
    final VBox vbox = createMessage(service);

    System.out.println("[FINE] ProgramController.registerService() - registering new service: " + service);

    service.stateProperty().addListener(new ChangeListener<State>() {
      @Override
      public void changed(ObservableValue<? extends State> observableValue, State oldValue, State newValue) {
        if(newValue == State.SCHEDULED) {
          messagePane.getChildren().add(vbox);
        }
        else if(newValue == State.SUCCEEDED || newValue == State.FAILED || newValue == State.CANCELLED) {
          messagePane.getChildren().remove(vbox);
        }
      }
    });
  }

  public void showOptionScreen(final String title, final List<? extends Option> options) {
    navigator.navigateToModal(new Destination(title) {
      private DialogScreen dialogScreen;

      @Override
      public void go() {
        dialogScreen = new DialogScreen(title, options);

        sceneRoot.getChildren().add(dialogScreen);

        dialogScreen.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
          @Override
          public void handle(KeyEvent event) {
            if(BACK_SPACE.match(event)) {
              navigator.back();
            }
            event.consume();
          }
        });
        dialogScreen.requestFocus();
      }

      @Override
      public void outro() {
        sceneRoot.getChildren().remove(dialogScreen);
      }
    });
  }

  private static VBox createMessage(final Worker<?> worker) {
    return new VBox() {{
      getChildren().add(new VBox() {{
        getStyleClass().add("item");
        getChildren().add(new Label("Title") {{
          getStyleClass().add("title");
          textProperty().bind(worker.titleProperty());
        }});
        getChildren().add(new Label() {{
          setWrapText(true);
          setMaxWidth(300);
          getStyleClass().add("description");
          textProperty().bind(worker.messageProperty());
        }});
        getChildren().add(new ProgressBar() {{
          getStyleClass().add("blue-bar");
          setMaxWidth(300);
          progressProperty().bind(worker.progressProperty());
        }});
      }});
    }};
  }

  // SelectItemScene...
  // NavigationInterface
  //  - back();
  //  - play();
  //  - editItem();
  //  - castInfo();
  //
  // MainNavInterface
  //  - selectItem();
  //  - exitProgram();
  //
  // TransparentPlayingNavInterface
  //  - back();
  //  - selectSubtitle();
}
