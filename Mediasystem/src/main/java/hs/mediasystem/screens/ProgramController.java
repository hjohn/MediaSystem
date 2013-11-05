package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.player.PlayerEvent;
import hs.mediasystem.screens.collection.CollectionPresentation;
import hs.mediasystem.screens.main.MainScreenLocation;
import hs.mediasystem.screens.playback.PlaybackLocation;
import hs.mediasystem.screens.playback.PlaybackOverlayPane;
import hs.mediasystem.screens.playback.PlaybackOverlayPresentation;
import hs.mediasystem.screens.playback.PlayerPresentation;
import hs.mediasystem.screens.playback.SubtitleDownloadService;
import hs.mediasystem.util.Dialog;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.SceneUtil;
import hs.mediasystem.util.annotation.Nullable;
import hs.mediasystem.util.ini.Ini;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
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

  private static final MainScreenLocation MAIN_SCREEN_LOCATION = new MainScreenLocation();

  private final Scene scene;
  private final StackPane sceneRoot = new StackPane();
  private final BorderPane videoPane = new BorderPane();
  private final BorderPane contentBorderPane = new BorderPane();
  private final BorderPane informationBorderPane = new BorderPane();
  private final BorderPane messageBorderPane = new BorderPane();
  private final Ini ini;
  private final SubtitleDownloadService subtitleDownloadService = new SubtitleDownloadService();
  private final SceneManager sceneManager;
  private final PlayerPresentation playerPresentation;

  private final VBox messagePane = new VBox() {{
    getStylesheets().add("status-messages.css");
    getStyleClass().add("status-messages");
    setVisible(false);
  }};

  private final InformationBorder informationBorder;

  private final Map<Class<?>, Map<KeyCombination, Action<?>>> actionsByKeyCombinationByPresentation = new HashMap<>();

  private ResizableWritableImageView videoCanvas;

  private final ObjectProperty<Location> location = new SimpleObjectProperty<>();
  public Location getLocation() { return location.get(); }
  public void setLocation(Location location) { this.location.set(location); }
  public ObjectProperty<Location> locationProperty() { return location; }

  private final InvalidationListener videoSizeInvalidationListener = new InvalidationListener() {
    @Override
    public void invalidated(Observable observable) {
      if(videoCanvas != null) {
        System.out.println("[FINE] ProgramController: CanvasSize: " + videoCanvas.getWidth() + "x" + videoCanvas.getHeight() + " PaneSize: " + videoPane.getWidth() + "x" + videoPane.getHeight());

        double scaleX = videoPane.getWidth() / videoCanvas.getWidth();
        double scaleY = videoPane.getHeight() / videoCanvas.getHeight();

        if(scaleX > scaleY) {
          scaleX = scaleY;
        }
        else {
          scaleY = scaleX;
        }

        videoCanvas.setScaleX(scaleX);
        videoCanvas.setScaleY(scaleY);
        videoCanvas.relocate((videoPane.getWidth() - videoCanvas.getWidth()) / 2, (videoPane.getHeight() - videoCanvas.getHeight()) / 2);
      }
    }
  };

  private Layout<? extends Location, MainLocationPresentation> currentLayout;
  private MainLocationPresentation activePresentation;

  @Inject
  public ProgramController(Ini ini, final SceneManager sceneManager, @Nullable final PlayerPresentation playerPresentation, InformationBorder informationBorder, Provider<Set<Layout<? extends Location, ? extends MainLocationPresentation>>> mainLocationLayoutsProvider) {
    this.ini = ini;
    this.sceneManager = sceneManager;
    this.playerPresentation = playerPresentation;
    this.informationBorder = informationBorder;
    this.scene = SceneUtil.createScene(sceneRoot);

    sceneRoot.getChildren().addAll(videoPane, contentBorderPane, informationBorderPane, messageBorderPane);

    Object displayComponent = playerPresentation == null ? null : playerPresentation.getPlayer().getDisplayComponent();

    if(displayComponent instanceof ResizableWritableImageView) {
      videoCanvas = (ResizableWritableImageView)displayComponent;
      videoPane.setCenter(videoCanvas);
      videoCanvas.setManaged(false);

      videoCanvas.widthProperty().addListener(videoSizeInvalidationListener);
      videoCanvas.heightProperty().addListener(videoSizeInvalidationListener);
      videoPane.widthProperty().addListener(videoSizeInvalidationListener);
      videoPane.heightProperty().addListener(videoSizeInvalidationListener);
    }

    initializeKeyMappings();

    sceneManager.setScene(scene);

    informationBorderPane.setPickOnBounds(false);
    informationBorderPane.setTop(informationBorder);
    messageBorderPane.setMouseTransparent(true);
    messageBorderPane.setRight(new Group(messagePane));

    scene.addEventHandler(NavigationEvent.NAVIGATION_ANCESTOR, new EventHandler<NavigationEvent>() {
      @Override
      public void handle(NavigationEvent event) {
        if(event.getEventType() == NavigationEvent.NAVIGATION_BACK) {
          Location parent = getLocation().getParent();

          setLocation(parent == null ? MAIN_SCREEN_LOCATION : parent);
          event.consume();
        }
        else if(playerPresentation != null && getActiveScreen().getClass() == PlaybackOverlayPane.class) {
          playerPresentation.getPlayer().onPlayerEvent().set(null);

          setLocation(getLocation().getParent());

          informationBorder.setVisible(true);

          playerPresentation.stop();
          sceneManager.disposePlayerRoot();

          subtitleDownloadService.cancel();
        }
      }
    });

    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          scene.getFocusOwner().fireEvent(new NavigationEvent(NavigationEvent.NAVIGATION_BACK));
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
          event.consume();
        }
        else if(getActiveScreen().getClass() == PlaybackOverlayPane.class) {
          if(KEY_S.match(event)) {
            scene.getFocusOwner().fireEvent(new NavigationEvent(NavigationEvent.NAVIGATION_EXIT));
            event.consume();
          }

        }

        if(!event.isConsumed()) {
          handleUserDefinedKeys(event);
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

    location.addListener(new ChangeListener<Location>() {
      @Override
      public void changed(ObservableValue<? extends Location> observable, Location old, Location current) {
        System.out.println("[INFO] Changing Location" + (old == null ? "" : " from " + old.getId()) + " to " + current.getId());

        @SuppressWarnings("unchecked")
        Layout<? extends Location, MainLocationPresentation> layout = (Layout<? extends Location, MainLocationPresentation>)Layout.findMostSuitableLayout(mainLocationLayoutsProvider.get(), current.getClass());

        ProgramController.this.informationBorder.breadCrumbProperty().set(current.getBreadCrumb());

        if(layout != null && layout.equals(currentLayout)) {
          return;
        }

        currentLayout = layout;

        if(activePresentation != null) {
          activePresentation.location.unbindBidirectional(location);
          activePresentation = null;
        }

        if(currentLayout != null) {
          activePresentation = currentLayout.createPresentation();
          activePresentation.location.bindBidirectional(location);

          displayOnStage(currentLayout.createView(activePresentation), current.getType());
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  private Action<?> findAction(String actionName) {
    int lastDot = actionName.lastIndexOf('.');

    try {
      return (Action<?>)Enum.valueOf(Class.forName(actionName.substring(0, lastDot)).asSubclass(Enum.class), actionName.substring(lastDot + 1));
    }
    catch(ClassNotFoundException | IllegalArgumentException e) {
      return null;
    }
  }

  private void initializeKeyMappings() {
    addKeyMapping(CollectionPresentation.class, new KeyCodeCombination(KeyCode.F), "hs.mediasystem.screens.collection.CollectionActions.FILTER_SHOW_DIALOG");
    addKeyMapping(CollectionPresentation.class, new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN), "hs.mediasystem.screens.collection.CollectionActions.GROUP_SET_NEXT");

    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.DIGIT9), "hs.mediasystem.screens.playback.PlayerActions.VOLUME_DECREASE");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.DIGIT0), "hs.mediasystem.screens.playback.PlayerActions.VOLUME_INCREASE");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.M), "hs.mediasystem.screens.playback.PlayerActions.MUTE");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.SPACE), "hs.mediasystem.screens.playback.PlayerActions.PAUSE");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.LEFT), "hs.mediasystem.screens.playback.PlayerActions.SKIP_BACKWARD_10S");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.NUMPAD4), "hs.mediasystem.screens.playback.PlayerActions.SKIP_BACKWARD_10S");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.RIGHT), "hs.mediasystem.screens.playback.PlayerActions.SKIP_FORWARD_10S");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.NUMPAD6), "hs.mediasystem.screens.playback.PlayerActions.SKIP_FORWARD_10S");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.DOWN), "hs.mediasystem.screens.playback.PlayerActions.SKIP_BACKWARD_60S");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.NUMPAD2), "hs.mediasystem.screens.playback.PlayerActions.SKIP_BACKWARD_60S");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.UP), "hs.mediasystem.screens.playback.PlayerActions.SKIP_FORWARD_60S");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.NUMPAD8), "hs.mediasystem.screens.playback.PlayerActions.SKIP_FORWARD_60S");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.DIGIT1), "hs.mediasystem.screens.playback.PlayerActions.BRIGHTNESS_DECREASE");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.DIGIT2), "hs.mediasystem.screens.playback.PlayerActions.BRIGHTNESS_INCREASE");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.Z), "hs.mediasystem.screens.playback.PlayerActions.SUBTITLE_DELAY_DECREASE");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.X), "hs.mediasystem.screens.playback.PlayerActions.SUBTITLE_DELAY_INCREASE");
    addKeyMapping(PlayerPresentation.class, KEY_OPEN_BRACKET, "hs.mediasystem.screens.playback.PlayerActions.RATE_DECREASE");
    addKeyMapping(PlayerPresentation.class, KEY_CLOSE_BRACKET, "hs.mediasystem.screens.playback.PlayerActions.RATE_INCREASE");
    addKeyMapping(PlayerPresentation.class, new KeyCodeCombination(KeyCode.J), "hs.mediasystem.screens.playback.PlayerActions.SUBTITLE_NEXT");
    addKeyMapping(PlaybackOverlayPresentation.class, new KeyCodeCombination(KeyCode.I), "hs.mediasystem.screens.playback.PlaybackOverlayActions.VISIBILITY");
  }

  private void addKeyMapping(Class<?> presentationClass, KeyCombination keyCombination, String actionName) {
    Action<?> action = findAction(actionName);

    if(action == null) {
      System.out.println("[WARN] Cannot find associated Action for key combination [" + keyCombination + "]: " + actionName);
    }
    else {
      Map<KeyCombination, Action<?>> actionsByKeyCombination = actionsByKeyCombinationByPresentation.get(presentationClass);

      if(actionsByKeyCombination == null) {
        actionsByKeyCombination = new HashMap<>();
        actionsByKeyCombinationByPresentation.put(presentationClass, actionsByKeyCombination);
      }

      actionsByKeyCombination.put(keyCombination, action);
    }
  }

  private void handleUserDefinedKeys(KeyEvent event) {

    /*
     * Handling of user defined key combinations:
     * - Check up the chain from the event target to find relevant presentations
     * - Check each presentation in turn for potential actions
     */

    EventTarget currentEventChainNode = event.getTarget();

    while(currentEventChainNode != null) {
      if(currentEventChainNode instanceof PresentationPane) {
        PresentationPane pane = (PresentationPane)currentEventChainNode;
        Object presentation = pane.getPresentation();

        if(handleUserDefinedKeysForPresentation(event, presentation)) {
          return;
        }
      }

      currentEventChainNode = currentEventChainNode instanceof Node ? ((Node)currentEventChainNode).getParent() : null;
    }

    if(handleUserDefinedKeysForPresentation(event, activePresentation)) {
      return;
    }

    handleUserDefinedKeysForPresentation(event, playerPresentation);
  }

  private boolean handleUserDefinedKeysForPresentation(KeyEvent event, Object presentation) {
    System.out.println("Handling user defined key... " + event);
    Map<KeyCombination, Action<?>> actionsByKeyCombination = actionsByKeyCombinationByPresentation.get(presentation.getClass());

    if(actionsByKeyCombination != null) {
      for(KeyCombination keyCombination : actionsByKeyCombination.keySet()) {
        if(keyCombination.match(event)) {
          @SuppressWarnings("unchecked")
          Action<Object> action = (Action<Object>)actionsByKeyCombination.get(keyCombination);

          action.perform(presentation);
          return true;
        }
      }
    }

    return false;
  }

  public Ini getIni() {
    return ini;
  }

  private void displayOnMainStage(Node node) {
    displayOnStage(node, Location.Type.NORMAL);
  }

  private void displayOnStage(final Node node, Location.Type type) {
    Color background = Color.BLACK;

    if(videoCanvas == null && type == Location.Type.PLAYBACK) {
      background = Color.TRANSPARENT;
    }

    if(videoCanvas != null) {
      //videoCanvas.getGraphicsContext2D().clearRect(0, 0, videoCanvas.getWidth(), videoCanvas.getHeight());
      videoCanvas.setVisible(type == Location.Type.PLAYBACK);
    }

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
    location.set(MAIN_SCREEN_LOCATION);
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

    if(mediaItem.mediaData.get() != null) {
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
        if(event.getType() == PlayerEvent.Type.FINISHED) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              scene.getFocusOwner().fireEvent(new NavigationEvent(NavigationEvent.NAVIGATION_EXIT));
            }
          });
          event.consume();
        }
      }
    });
    playerPresentation.getPlayer().positionProperty().set(0);
    playerPresentation.play(mediaItem.getUri(), positionMillis);
    currentMediaItem = mediaItem;

    setLocation(new PlaybackLocation(getLocation(), positionMillis));

    informationBorder.setVisible(false);
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

  public void showDialog(Dialog<?> dialog) {
    sceneManager.displayDialog(dialog);
  }

  public <R> R showSynchronousDialog(Dialog<R> dialog) {
    return sceneManager.displaySynchronousDialog(dialog);
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
