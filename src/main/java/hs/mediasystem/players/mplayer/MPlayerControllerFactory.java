package hs.mediasystem.players.mplayer;

import hs.mediasystem.Controller;
import hs.mediasystem.ControllerFactory;
import hs.ui.frames.Frame;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.Window;
import java.nio.file.Path;

import javax.swing.JFrame;

/**
 * Handles the main window(s) and media playback
 */
public class MPlayerControllerFactory implements ControllerFactory {
  private final Path mplayerPath;

  public MPlayerControllerFactory(Path mplayerPath) {
    this.mplayerPath = mplayerPath;
  }
  
  @Override
  public Controller create(GraphicsDevice device) {
    Frame overlayFrame = new Frame();
    
    device.setFullScreenWindow((JFrame)overlayFrame.getContainer());
    
//    ((JFrame)overlayFrame.getContainer()).setLocationByPlatform(true);
    ((JFrame)overlayFrame.getContainer()).setAlwaysOnTop(true);
    ((JFrame)overlayFrame.getContainer()).setUndecorated(true);
    ((Window)overlayFrame.getContainer()).setBackground(new Color(0, 0, 0, 1));
    
//    overlayFrame.visible().set(true);

//    overlayFrame.maximized().set(true);
    overlayFrame.visible().set(true);
    
    return new Controller(new MPlayerPlayer(mplayerPath, true), overlayFrame);
  }
}
