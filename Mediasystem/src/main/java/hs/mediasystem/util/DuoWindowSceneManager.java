package hs.mediasystem.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DuoWindowSceneManager implements SceneManager {
  private final Stage mainStage;  // WORKAROUND: Two stages because a transparent mainstage performs so poorly; only using a transparent stage when media is playing; refactor this
  private final Stage transparentStage;

  private Frame playerFrame;

  private final ChangeListener<Paint> fillChangeListener = new ChangeListener<Paint>() {
    @Override
    public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
      display();
    }
  };

  private Scene scene;
  private int screenNumber;

  public DuoWindowSceneManager(String title, int initialScreenNumber) {
    this.screenNumber = initialScreenNumber;

    mainStage = new Stage(StageStyle.UNDECORATED);
    transparentStage = new Stage(StageStyle.TRANSPARENT);

    mainStage.setTitle(title);
    transparentStage.setTitle(title);

    setPlayerScreen(initialScreenNumber);
  }

  @Override
  public void setScene(Scene scene) {
    if(this.scene != null) {
      this.scene.fillProperty().removeListener(fillChangeListener);
    }

    this.scene = scene;
    this.scene.fillProperty().addListener(fillChangeListener);

    display();
  }

  private Stage getPrimaryStage() {
    Paint paint = scene.getFill();

    if(paint instanceof Color && ((Color)paint).getOpacity() == 1.0) {
      return mainStage;
    }

    return transparentStage;
  }

  private void display() {
    Paint paint = scene.getFill();

    if(paint instanceof Color && ((Color)paint).getOpacity() == 1.0) {
      displayOnStage(mainStage, transparentStage);
    }
    else {
      displayOnStage(transparentStage, mainStage);
    }
  }

  private void displayOnStage(Stage newStage, Stage oldStage) {
    oldStage.setScene(null);

    newStage.setScene(scene);
    newStage.show();

    setupStageLocation(newStage);

    newStage.toFront();
    oldStage.hide();
  }

  @Override
  public void setPlayerRoot(Component playerDisplay) {
    playerFrame.removeAll();
    playerFrame.add(playerDisplay, BorderLayout.CENTER);
    playerFrame.doLayout();
  }

  @Override
  public void disposePlayerRoot() {
    playerFrame.removeAll();
  }

  @Override
  public int getScreenNumber() {
    return screenNumber;
  }

  @Override
  public void setScreenNumber(int screenNumber) {
    this.screenNumber = screenNumber;

    setPlayerScreen(screenNumber);

    setupStageLocation(mainStage);
    setupStageLocation(transparentStage);
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

  private void setPlayerScreen(int screenNumber) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();

    GraphicsDevice graphicsDevice = (screenNumber >= 0 && screenNumber < gs.length) ? gs[screenNumber] : gs[0];

    if(playerFrame == null) {
      playerFrame = new Frame(graphicsDevice.getDefaultConfiguration());
      playerFrame.setLayout(new BorderLayout());
      playerFrame.setUndecorated(true);
      playerFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
      playerFrame.setBackground(new java.awt.Color(0, 0, 0));
      playerFrame.setVisible(true);
    }
    else {
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
