package hs.mediasystem;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Player;
import hs.mediasystem.screens.MainScreen;
import hs.mediasystem.screens.TransparentPlayingScreen;
import hs.mediasystem.util.ini.Ini;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ProgramController {
  private final Player player;
  private final Stage mainStage;  // TODO two stages because a transparent mainstage performs so poorly; only using a transparent stage when media is playing
  private final Stage overlayStage;
  private final BorderPane mainGroup;
  private final BorderPane overlayGroup;
  private final Ini ini;
  
  public ProgramController(Ini ini, Player player) {
    this.ini = ini;
    this.player = player;
    
    mainGroup = new BorderPane();
    overlayGroup = new BorderPane();
    
    mainStage = new Stage(StageStyle.UNDECORATED);
    overlayStage = new Stage(StageStyle.TRANSPARENT);

    setupStage(mainStage, mainGroup, 1.0);
    setupStage(overlayStage, overlayGroup, 0.05);
  }
  
  public Ini getIni() {
    return ini;
  }
  
  private static void setupStage(Stage stage, BorderPane borderPane, double transparency) {
    Scene scene = new Scene(borderPane, new Color(0, 0, 0, transparency));

    stage.setScene(scene);
    
    Screen screen = Screen.getPrimary();
    Rectangle2D bounds = screen.getVisualBounds();

    stage.setX(bounds.getMinX());
    stage.setY(bounds.getMinY());
    stage.setWidth(bounds.getWidth());
    stage.setHeight(bounds.getHeight());

    scene.getStylesheets().add("default.css");
  }
  
  private void displayOnMainStage(Node node) {
    mainStage.show();
    overlayStage.hide();
    mainGroup.setCenter(node);
  }
  
  private void displayOnOverlayStage(Node node) {
    overlayStage.show();
    mainStage.hide();
    overlayGroup.setCenter(node);
  }
  
  public void showMainScreen() {
    MainScreen mainScreen = new MainScreen(this);
    
    displayOnMainStage(mainScreen.create());
  }
  
  public void showSelectItemScreen(MediaTree mediaTree) {
    SelectItemScene selectItemScreen = new SelectItemScene(this);
    
    displayOnMainStage(selectItemScreen.create(mediaTree));
  }
  
  public void play(MediaItem mediaItem) {
    player.play(mediaItem.getUri());
    
    TransparentPlayingScreen screen = new TransparentPlayingScreen(this);
    displayOnOverlayStage(screen.create(mediaItem));
  }
  
  public void stop() {
    mainStage.show();
    overlayStage.hide();
    player.stop();
  }
}
