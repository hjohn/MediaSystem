package hs.mediasystem.screens;

import hs.mediasystem.ProgramController;
import hs.mediasystem.framework.MediaItem;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class TransparentPlayingScreen {
  private final ProgramController controller;

  public TransparentPlayingScreen(ProgramController controller) {
    this.controller = controller;
  }

  public Node create(MediaItem mediaItem) {
    Group group = new Group();
    
    group.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        KeyCode code = event.getCode();
        
        if(code == KeyCode.S) {
          controller.stop();
        }
        else if(code == KeyCode.SPACE) {
          controller.pause();
        }
        else if(code == KeyCode.NUMPAD4) {
          controller.move(-10 * 1000);
        }
        else if(code == KeyCode.NUMPAD6) {
          controller.move(10 * 1000);
        }
        else if(code == KeyCode.NUMPAD2) {
          controller.move(-60 * 1000);
        }
        else if(code == KeyCode.NUMPAD8) {
          controller.move(60 * 1000);
        }
        else if(code == KeyCode.M) {
          controller.mute();
        }
        else if(code == KeyCode.DIGIT9) {
          controller.changeVolume(-1);
        }
        else if(code == KeyCode.DIGIT0) {
          controller.changeVolume(1);
        }
        else if(code == KeyCode.DIGIT1) {
          controller.changeBrightness(-0.05f);
        }
        else if(code == KeyCode.DIGIT2) {
          controller.changeBrightness(0.05f);
        }
        else if(code == KeyCode.Z) {
          controller.changeSubtitleDelay(-100);
        }
        else if(code == KeyCode.X) {
          controller.changeSubtitleDelay(100);
        }
      }
    });
    
    group.setFocusTraversable(true);
    
    return group;
  }

}
