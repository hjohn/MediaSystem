package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SublightSubtitleProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
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
  private final Player player;
  private final Stage mainStage;  // TODO two stages because a transparent mainstage performs so poorly; only using a transparent stage when media is playing; refactor this
  private final Stage overlayStage;
  private final StackPane mainStackPane = new StackPane();
  private final StackPane transparentStackPane = new StackPane();
  private final BorderPane mainGroup = new BorderPane();
  private final BorderPane transparentGroup = new BorderPane();
  private final BorderPane mainOverlay = new BorderPane();
  private final BorderPane transparentOverlay = new BorderPane();
  private final Ini ini;
  private final Provider<MainScreen> mainScreenProvider;
  private final Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider;
  private final SubtitleDownloadService subtitleDownloadService = new SubtitleDownloadService();

  private final VBox messagePane = new VBox() {{
    setId("status-messages");
    setVisible(false);
  }};

  private final NavigationHistory<NavigationItem> history = new NavigationHistory<>();

  @Inject
  public ProgramController(Ini ini, Player player, Provider<MainScreen> mainScreenProvider, Provider<PlaybackOverlayPresentation> playbackOverlayPresentationProvider) {
    this.ini = ini;
    this.player = player;
    this.mainScreenProvider = mainScreenProvider;
    this.playbackOverlayPresentationProvider = playbackOverlayPresentationProvider;

    mainStackPane.getChildren().addAll(mainGroup, mainOverlay);
    transparentStackPane.getChildren().addAll(transparentGroup, transparentOverlay);

    mainOverlay.setMouseTransparent(true);
    transparentOverlay.setMouseTransparent(true);

    mainStage = new Stage(StageStyle.UNDECORATED);
    mainStage.setFullScreen(true);
    overlayStage = new Stage(StageStyle.TRANSPARENT);
    overlayStage.setFullScreen(true);

    setupStage(mainStage, mainStackPane, 1.0);
    setupStage(overlayStage, transparentStackPane, 0.0);

//    mainGroup.addEventHandler(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
//      @Override
//      public void handle(KeyEvent event) {
//        System.out.println(event);
//
//      }
//    });

    mainGroup.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(event.getCode().equals(KeyCode.BACK_SPACE)) {
          history.back();
        }
      }
    });

    history.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        updateScreen();
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

  private static void setupStage(Stage stage, Parent parent, double transparency) {
    Scene scene = new Scene(parent, new Color(0, 0, 0, transparency));

    stage.setScene(scene);

    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();

    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());
    stage.setWidth(bounds.getWidth());
    stage.setHeight(bounds.getHeight());
  }

  private void displayOnMainStage(Node node) {
    ObservableList<String> stylesheets = mainStage.getScene().getStylesheets();

    stylesheets.clear();
    stylesheets.addAll("default.css", "status-messages.css", node.getId() + ".css");

    mainStage.show();
    mainStage.setFullScreen(true);
    mainStage.toFront();
    overlayStage.hide();
    transparentOverlay.setRight(null);
    mainOverlay.setRight(messagePane);
    mainGroup.setCenter(node);
  }

  private void displayOnOverlayStage(Node node) {
    ObservableList<String> stylesheets = overlayStage.getScene().getStylesheets();

    stylesheets.clear();
    stylesheets.addAll("default.css", "status-messages.css", node.getId() + ".css");

    overlayStage.show();
    overlayStage.setFullScreen(true);
    overlayStage.toFront();
    mainStage.hide();
    mainOverlay.setRight(null);
    transparentOverlay.setRight(messagePane);
    transparentGroup.setCenter(node);
  }

  public void showMainScreen() {
    MainScreen mainScreen = mainScreenProvider.get();

    history.forward(new NavigationItem(mainScreen.create(), "MAIN"));
  }

  public void showScreen(Node node) {
    history.forward(new NavigationItem(node, "MAIN"));
  }

  private void updateScreen() {
    if(history.current().getStage().equals("MAIN")) {
      displayOnMainStage(history.current().getNode());
    }
    else {
      displayOnOverlayStage(history.current().getNode());
    }
  }

  private MediaItem currentMediaItem;

  public MediaItem getCurrentMediaItem() {
    return currentMediaItem;
  }

  public void play(MediaItem mediaItem) {
    player.play(mediaItem.getUri());

    currentMediaItem = mediaItem;

    PlaybackOverlayPresentation playbackOverlayPresentation = playbackOverlayPresentationProvider.get();
    history.forward(new NavigationItem(playbackOverlayPresentation.getView(), "OVERLAY"));
  }

  public void stop() {
    subtitleDownloadService.cancel();
    player.stop();
    history.back();
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
