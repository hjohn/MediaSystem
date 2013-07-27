package hs.mediasystem.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DefaultSceneManager implements SceneManager {
  private final Stage mainStage;

  private Frame playerFrame;
  private Scene scene;
  private int screenNumber;

  public DefaultSceneManager(String title, int initialScreenNumber) {
    this.screenNumber = initialScreenNumber;

    mainStage = new Stage(StageStyle.TRANSPARENT);
    mainStage.setTitle(title);
  }

  @Override
  public void setScene(Scene scene) {
    this.scene = scene;

    display();
  }

  private Stage getPrimaryStage() {
    return mainStage;
  }

  private void display() {
    mainStage.setScene(scene);
    mainStage.show();

    setupStageLocation(mainStage);

    mainStage.toFront();
  }

  @Override
  public void setPlayerRoot(Object playerDisplay) {
    if(playerDisplay instanceof Component) {

      /*
       * AWT node, put on seperate window
       */

      createPlayerFrame();

      playerFrame.removeAll();
      playerFrame.add((Component)playerDisplay, BorderLayout.CENTER);
      playerFrame.doLayout();
    }
  }

  @Override
  public void disposePlayerRoot() {
    destroyPlayerFrame();
  }

  @Override
  public int getScreenNumber() {
    return screenNumber;
  }

  @Override
  public void setScreenNumber(int screenNumber) {
    this.screenNumber = screenNumber;

    setPlayerScreen();

    setupStageLocation(mainStage);
  }

  private void setupStageLocation(Stage stage) {
    ObservableList<Screen> screens = Screen.getScreens();
    Screen screen = screens.size() <= screenNumber ? Screen.getPrimary() : screens.get(screenNumber);

    Rectangle2D bounds = screen.getBounds();
    boolean primary = screen.equals(Screen.getPrimary());    // WORKAROUND: this doesn't work nice in combination with full screen, so this hack is used to prevent going fullscreen when screen is not primary

    if(primary) {
      stage.setX(bounds.getMinX());
      stage.setY(bounds.getMinY());
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());
      stage.setFullScreen(true);
    }
    else {
      stage.setX(bounds.getMinX());
      stage.setY(bounds.getMinY());
      stage.setWidth(bounds.getWidth());
      stage.setHeight(bounds.getHeight());
      stage.toFront();
    }
  }

  private void createPlayerFrame() {
    if(playerFrame == null) {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] gs = ge.getScreenDevices();

      GraphicsDevice graphicsDevice = (screenNumber >= 0 && screenNumber < gs.length) ? gs[screenNumber] : gs[0];

      playerFrame = new Frame(graphicsDevice.getDefaultConfiguration());
      playerFrame.setLayout(new BorderLayout());
      playerFrame.setUndecorated(true);
      playerFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
      playerFrame.setBackground(new java.awt.Color(0, 0, 0));
      playerFrame.setVisible(true);
    }
  }

  private void destroyPlayerFrame() {
    if(playerFrame != null) {
      playerFrame.dispose();
      playerFrame = null;
    }
  }

  private void setPlayerScreen() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();

    GraphicsDevice graphicsDevice = (screenNumber >= 0 && screenNumber < gs.length) ? gs[screenNumber] : gs[0];

    if(playerFrame != null) {
      Rectangle rectangle = graphicsDevice.getDefaultConfiguration().getBounds();

      playerFrame.setBounds(rectangle);
    }
  }

  @Override
  public void displayDialog(Dialog dialog) {
    dialog.showDialog(getPrimaryStage(), false);
  }

  @Override
  public void displaySynchronousDialog(Dialog dialog) {
    dialog.showDialog(getPrimaryStage(), true);
  }
}
