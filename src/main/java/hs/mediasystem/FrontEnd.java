package hs.mediasystem;

import hs.mediasystem.framework.Player;
import hs.mediasystem.players.vlc.VLCPlayer;
import hs.mediasystem.util.ini.Ini;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;

import javafx.application.Application;
import javafx.stage.Stage;

public class FrontEnd extends Application {
  private static final Ini INI = new Ini(new File("mediasystem.ini"));

  private Player player;
  
  @Override
  public void init() throws Exception {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();
    
    int screen = Integer.parseInt(INI.getSection("general").getDefault("screen", "0"));
    
    GraphicsDevice graphicsDevice = (screen >= 0 && screen < gs.length) ? gs[screen] : gs[0];
    
    System.out.println("Using display: " + graphicsDevice + "; " + graphicsDevice.getDisplayMode().getWidth() + "x" + graphicsDevice.getDisplayMode().getHeight() + "x" + graphicsDevice.getDisplayMode().getBitDepth() + " @ " + graphicsDevice.getDisplayMode().getRefreshRate() + " Hz");

    player = new VLCPlayer(graphicsDevice);  // TODO make configurable
    System.out.println("1");
  }
  
  @Override
  public void start(Stage primaryStage) throws Exception {
    ProgramController controller = new ProgramController(player);
    
    controller.showMainScreen();
  }
}
