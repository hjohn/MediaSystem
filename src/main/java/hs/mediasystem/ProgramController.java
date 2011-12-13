package hs.mediasystem;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Player;
import hs.mediasystem.framework.Subtitle;
import hs.mediasystem.screens.MainScreen;
import hs.mediasystem.screens.TransparentPlayingScreen;
import hs.mediasystem.util.ini.Ini;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
  
  private final NavigationHistory<NavigationItem> history = new NavigationHistory<NavigationItem>();
  
  public ProgramController(Ini ini, Player player) {
    this.ini = ini;
    this.player = player;
    
    mainGroup = new BorderPane();
    overlayGroup = new BorderPane();
    
    mainStage = new Stage(StageStyle.UNDECORATED);
    overlayStage = new Stage(StageStyle.TRANSPARENT);
    overlayStage.setFullScreen(true);

    setupStage(mainStage, mainGroup, 1.0);
    setupStage(overlayStage, overlayGroup, 0.0);
    
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
    
    history.forward(new NavigationItem(mainScreen.create(), "MAIN"));
  }
  
  public void showSelectItemScreen(MediaTree mediaTree) {
    SelectItemScene selectItemScreen = new SelectItemScene(this);
    
    history.forward(new NavigationItem(selectItemScreen.create(mediaTree), "MAIN"));
  }
  
  private void updateScreen() {
    if(history.current().getStage().equals("MAIN")) {
      displayOnMainStage(history.current().getNode());
    }
    else {
      displayOnOverlayStage(history.current().getNode());
    }
  }
  
  public void play(MediaItem mediaItem) {
    player.play(mediaItem.getUri());
   
    TransparentPlayingScreen screen = new TransparentPlayingScreen(this);
    history.forward(new NavigationItem(screen.create(mediaItem, overlayStage.getWidth(), overlayStage.getHeight()), "OVERLAY"));
  }
  
  public void stop() {
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

  public Subtitle nextSubtitle() {
    List<Subtitle> subtitles = player.getSubtitles();
    
    if(subtitles.isEmpty()) {
      return null;
    }
    
    Subtitle currentSubtitle = player.getSubtitle();
    int index = currentSubtitle == null ? 1 : subtitles.indexOf(currentSubtitle) + 1;
    
    if(index >= subtitles.size()) {
      index = 0;
    }

    player.setSubtitle(subtitles.get(index));
    
    return subtitles.get(index);
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
