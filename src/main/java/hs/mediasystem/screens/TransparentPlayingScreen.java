package hs.mediasystem.screens;

import hs.mediasystem.ProgramController;
import hs.mediasystem.framework.MediaItem;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;

public class TransparentPlayingScreen {
  private final ProgramController controller;

  public TransparentPlayingScreen(ProgramController controller) {
    this.controller = controller;
  }

  public Node create(MediaItem mediaItem) {
    Group group = new Group();
    
    group.addEventHandler(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(event.getCharacter().equals("s")) {
          controller.stop();
        }
      }
    });
    
    group.setFocusTraversable(true);
    
    return group;
  }

}
