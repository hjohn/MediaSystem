package hs.mediasystem.screens;

import hs.mediasystem.config.KeyMappingsConfiguration;
import hs.mediasystem.controls.TablePane;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.actions.ActionTarget;
import hs.mediasystem.framework.actions.ActionTargetProvider;
import hs.mediasystem.framework.actions.controls.ActiveControlsProvider;
import hs.mediasystem.framework.actions.controls.NodeFactory;
import hs.mediasystem.framework.player.PlayerEvent;
import hs.mediasystem.screens.main.MainScreenLocation;
import hs.mediasystem.screens.playback.PlaybackLocation;
import hs.mediasystem.screens.playback.PlaybackOverlayPane;
import hs.mediasystem.screens.playback.PlayerPresentation;
import hs.mediasystem.screens.playback.SubtitleDownloadService;
import hs.mediasystem.util.DialogPane;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.SceneUtil;
import hs.mediasystem.util.annotation.Nullable;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.javafx.Dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
import javafx.scene.input.KeyCombination.Modifier;
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
  private static final Logger LOGGER = Logger.getLogger(ProgramController.class.getName());
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);
  private static final KeyCombination KEY_S = new KeyCodeCombination(KeyCode.S);
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);
  private static final KeyCombination KEY_CTRL_ALT_S = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);

  private static final MainScreenLocation MAIN_SCREEN_LOCATION = new MainScreenLocation();

  private final Scene scene;
  private final StackPane sceneRoot = new StackPane();
  private final BorderPane videoPane = new BorderPane();
  private final PresentationPane contentPresentationPane = new PresentationPane() {
    @Override
    public Object getPresentation() {
      return activePresentation;
    }
  };
  private final BorderPane informationBorderPane = new BorderPane();
  private final BorderPane messageBorderPane = new BorderPane();
  private final Ini ini;
  private final SubtitleDownloadService subtitleDownloadService = new SubtitleDownloadService();
  private final SceneManager sceneManager;
  private final PlayerPresentation playerPresentation;
  private final ActionTargetProvider actionControlFactory;
  private final ActiveControlsProvider activeControlsProvider;

  private final VBox messagePane = new VBox() {{
    getStylesheets().add("status-messages.css");
    getStyleClass().add("status-messages");
    setVisible(false);
  }};

  private final InformationBorder informationBorder;

  private final Map<KeyCodeCombination, List<String>> actionKeysByKeyCodeCombination;

  private ResizableWritableImageView videoCanvas;

  private final ObjectProperty<Location> location = new SimpleObjectProperty<>();
  public Location getLocation() { return location.get(); }
  public void setLocation(Location location) { this.location.set(location); }
  public ObjectProperty<Location> locationProperty() { return location; }

  private final InvalidationListener videoSizeInvalidationListener = new InvalidationListener() {
    @Override
    public void invalidated(Observable observable) {
      if(videoCanvas != null) {
        LOGGER.info("ProgramController: CanvasSize: " + videoCanvas.getWidth() + "x" + videoCanvas.getHeight() + " PaneSize: " + videoPane.getWidth() + "x" + videoPane.getHeight());

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

  private Layout<? extends Location, MainLocationPresentation<Location>> currentLayout;
  private MainLocationPresentation<Location> activePresentation;

  private boolean optionsVisible;

  @Inject
  public ProgramController(Ini ini, final SceneManager sceneManager, @Nullable final PlayerPresentation playerPresentation, InformationBorder informationBorder, Provider<Set<Layout<? extends Location, ? extends MainLocationPresentation<? extends Location>>>> mainLocationLayoutsProvider, KeyMappingsConfiguration keyMappingsConfiguration, ActionTargetProvider actionControlFactory, ActiveControlsProvider activeControlsProvider) {
    this.ini = ini;
    this.sceneManager = sceneManager;
    this.playerPresentation = playerPresentation;
    this.informationBorder = informationBorder;
    this.actionControlFactory = actionControlFactory;
    this.activeControlsProvider = activeControlsProvider;
    this.scene = SceneUtil.createScene(sceneRoot);

    sceneRoot.getChildren().addAll(videoPane, contentPresentationPane, informationBorderPane, messageBorderPane);

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

    actionKeysByKeyCodeCombination = keyMappingsConfiguration.getNewKeyMappings();

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

    scene.addEventHandler(LocationChangeEvent.LOCATION_CHANGE, new EventHandler<LocationChangeEvent>() {
      @Override
      public void handle(LocationChangeEvent event) {
        if(event.getLocation() instanceof PlaybackLocation) {
          // TODO this is a bit hacky, but makes it easy for now to call legacy play() function
          PlaybackLocation playbackLocation = (PlaybackLocation)event.getLocation();

          play(playbackLocation.getMedia());
        }
        else {
          setLocation(event.getLocation());
        }
      }
    });

    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        Node focusOwner = scene.getFocusOwner();

        if(BACK_SPACE.match(event)) {
          if(focusOwner == null) {
            focusOwner = scene.getRoot();
          }

          focusOwner.fireEvent(new NavigationEvent(NavigationEvent.NAVIGATION_BACK));  // TODO NPE here when nothing has focus (like on Info panel when there are no links)
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
        else if(KEY_O.match(event)) {
          if(!optionsVisible) {
            optionsVisible = true;
            handleOptions(event);
            event.consume();
          }
        }
        else if(getActiveScreen().getClass() == PlaybackOverlayPane.class) {
          if(KEY_S.match(event)) {
            focusOwner.fireEvent(new NavigationEvent(NavigationEvent.NAVIGATION_EXIT));
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
          LOGGER.info("Download of subtitle succeeded, setting subtitle to: " + subtitleDownloadService.getValue());
          playerPresentation.showSubtitle(subtitleDownloadService.getValue());
        }
      }
    });

    location.addListener(new ChangeListener<Location>() {
      @Override
      public void changed(ObservableValue<? extends Location> observable, Location old, Location current) {
        LOGGER.info("Changing Location" + (old == null ? "" : " from " + old.getId()) + " to " + current.getId());

        @SuppressWarnings("unchecked")
        Layout<? extends Location, MainLocationPresentation<Location>> layout = (Layout<? extends Location, MainLocationPresentation<Location>>)Layout.findMostSuitableLayout(mainLocationLayoutsProvider.get(), current.getClass());

        ProgramController.this.informationBorder.breadCrumbProperty().set(current.getBreadCrumb());

        if(layout == null || !layout.equals(currentLayout)) {
          currentLayout = layout;

          if(activePresentation != null) {
            activePresentation.dispose();
            activePresentation = null;
          }

          if(currentLayout != null) {
            activePresentation = currentLayout.createPresentation();

            displayOnStage(currentLayout.createView(activePresentation), current.getType());
          }
        }

        activePresentation.location.set(location.get());
      }
    });
  }

  private KeyCodeCombination keyEventToKeyCodeCombination(KeyEvent event) {
    List<Modifier> modifiers = new ArrayList<>();

    if(event.isControlDown()) {
      modifiers.add(KeyCombination.CONTROL_DOWN);
    }
    if(event.isAltDown()) {
      modifiers.add(KeyCombination.ALT_DOWN);
    }
    if(event.isShiftDown()) {
      modifiers.add(KeyCombination.SHIFT_DOWN);
    }
    if(event.isMetaDown()) {
      modifiers.add(KeyCombination.META_DOWN);
    }

    return new KeyCodeCombination(event.getCode(), modifiers.toArray(new Modifier[modifiers.size()]));
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

        if(handleNewKeyMappings(event, presentation)) {
          event.consume();
          return;
        }
      }

      currentEventChainNode = currentEventChainNode instanceof Node ? ((Node)currentEventChainNode).getParent() : null;
    }

    if(handleNewKeyMappings(event, playerPresentation)) {
      event.consume();
    }
  }

  private boolean handleNewKeyMappings(KeyEvent event, Object presentation) {
    if(!event.getCode().isModifierKey()) {
      List<String> actionKeys = actionKeysByKeyCodeCombination.get(keyEventToKeyCodeCombination(event));

      if(actionKeys != null) {
        for(String actionKey : actionKeys) {
          String propertyName = actionKey.substring(0, actionKey.indexOf(":"));

          for(ActionTarget actionTarget : actionControlFactory.getActionTargets(presentation)) {
            if(actionTarget.getMemberName().equals(propertyName)) {
              actionTarget.doAction(actionKey.substring(actionKey.indexOf(":") + 1), presentation, event);
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  private void handleOptions(KeyEvent event) {

    /*
     * Handling of options:
     * - Check up the chain from the event target to find relevant presentations
     * - Check each presentation in turn for potential actions
     */

    EventTarget currentEventChainNode = event.getTarget();

    TablePane tablePane = new TablePane();

    tablePane.getStyleClass().add("input-fields");

    while(currentEventChainNode != null) {
      if(currentEventChainNode instanceof PresentationPane) {
        PresentationPane pane = (PresentationPane)currentEventChainNode;
        Object presentation = pane.getPresentation();

        addControls(tablePane, presentation);
      }

      currentEventChainNode = currentEventChainNode instanceof Node ? ((Node)currentEventChainNode).getParent() : null;
    }

    if(getActiveScreen().getClass() == PlaybackOverlayPane.class) {
      addControls(tablePane, playerPresentation);
    }

    DialogPane<Void> dialogPane = new DialogPane<Void>() {{
      getChildren().add(tablePane);
    }};

    dialogPane.getStyleClass().add("media-look");

    Dialogs.showAndWait(event, dialogPane);

    optionsVisible = false;
  }

  private void addControls(TablePane tablePane, Object presentation) {
    for(NodeFactory controlFactory : activeControlsProvider.getControlFactories(presentation)) {
      String propertyName = controlFactory.getPropertyName();

      HBox hbox = createShortCut(propertyName);

      if(controlFactory.getLabel() != null) {
        hbox.getChildren().add(0, new Label(controlFactory.getLabel()));
      }

      tablePane.add(hbox);

      Node[] nodes = controlFactory.createNode(presentation);

      for(int i = 0; i < nodes.length; i++) {
        Node node = nodes[i];
        int columnSpan = 1;

        if(i == nodes.length - 1) {
          columnSpan = 5 - nodes.length;
        }

        tablePane.add(node, columnSpan);
      }

      tablePane.nextRow();
    }
  }

  private HBox createShortCut(String propertyName) {
    HBox hbox = new HBox();

    for(Map.Entry<KeyCodeCombination, List<String>> entry : actionKeysByKeyCodeCombination.entrySet()) {
      for(String actionKey : entry.getValue()) {
        if(actionKey.substring(0, actionKey.indexOf(":")).equals(propertyName)) {
          for(String label : entry.getKey().toString().split("\\+")) {
            Label keyLabel = new Label(label);

            keyLabel.getStyleClass().add("shortcut");
            hbox.getChildren().add(keyLabel);
          }
        }
      }
    }

    return hbox;
  }

  public Ini getIni() {
    return ini;
  }

  public Scene getScene() {
    return scene;
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

    contentPresentationPane.getChildren().setAll(node);
    scene.setFill(background);

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        node.requestFocus();
      }
    });
  }

  public Node getActiveScreen() {
    return contentPresentationPane.getChildren().get(0);
  }

  public void showMainScreen() {
    location.set(MAIN_SCREEN_LOCATION);
  }

  public void showScreen(final Node node) {
    assert node != null;

    displayOnMainStage(node);
  }

  private Media currentMedia;

  public Media getCurrentMedia() {
    return currentMedia;
  }

  private synchronized void play(final Media media) {
    if(playerPresentation == null) {
      Dialogs.show(scene, new InformationDialog("No video player was configured.\nUnable to play the selected item."));
      return;
    }

    Integer resumePosition = 0;

    if(media.getMediaItem().mediaData.get() != null) {
      resumePosition = Dialogs.showAndWait(scene, new ResumeDialog(media.getMediaItem()));

      if(resumePosition == null) {
        return;
      }
    }

    final int finalResumePosition = resumePosition;

    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), new KeyValue(scene.getRoot().opacityProperty(), 0.0)));

    timeline.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        startPlay(media, finalResumePosition * 1000L);
      }
    });
    timeline.play();
  }

  private void startPlay(final Media media, long positionMillis) {
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
    playerPresentation.play(media.getMediaItem().getUri(), positionMillis);
    currentMedia = media;

    setLocation(new PlaybackLocation(getLocation(), media, positionMillis));

    informationBorder.setVisible(false);
  }

  public SubtitleDownloadService getSubtitleDownloadService() {
    return subtitleDownloadService;
  }

  public final void registerWorker(final Worker<?> worker) {
    final Node node = createMessage(worker);

    LOGGER.fine("ProgramController.registerService() - registering new service: " + worker);

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
