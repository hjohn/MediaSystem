package hs.mediasystem.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
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

  private final ChangeListener<Paint> fillChangeListener = new ChangeListener<Paint>() {
    @Override
    public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
      display();
    }
  };

  private Frame playerFrame;
  private Scene scene;
  private int screenNumber;

  public DuoWindowSceneManager(String title, int initialScreenNumber) {
    this.screenNumber = initialScreenNumber;

    mainStage = new Stage(StageStyle.UNDECORATED);
    transparentStage = new Stage(StageStyle.TRANSPARENT);

    mainStage.setTitle(title);
    transparentStage.setTitle(title);
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

    setupStageLocation(newStage, determineScreen());

    newStage.toFront();
    oldStage.hide();
  }

  @Override
  public void setPlayerRoot(Object playerDisplay) {
    if(playerDisplay instanceof Component) {

      /*
       * AWT node, put on seperate window
       */

      createPlayerFrame(determineScreen());

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

    Screen screen = determineScreen();

    setPlayerScreen(screen);
    setupStageLocation(mainStage, screen);
    setupStageLocation(transparentStage, screen);
  }

  private Screen determineScreen() {
    ObservableList<Screen> screens = Screen.getScreens();

    return screens.size() <= screenNumber ? Screen.getPrimary() : screens.get(screenNumber);
  }

  private void setupStageLocation(Stage stage, Screen screen) {
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

  private void createPlayerFrame(Screen screen) {
    if(playerFrame == null) {
      playerFrame = new Frame();
      playerFrame.setLayout(new BorderLayout());
      playerFrame.setUndecorated(true);
      playerFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
      playerFrame.setBackground(new java.awt.Color(0, 0, 0));
      playerFrame.setVisible(true);

      setPlayerScreen(screen);
    }
  }

  private void destroyPlayerFrame() {
    if(playerFrame != null) {
      playerFrame.removeAll();
      playerFrame.dispose();
      playerFrame = null;
    }
  }

  private void setPlayerScreen(Screen screen) {
    if(playerFrame != null) {
      Rectangle bounds = new Rectangle(
        (int)screen.getBounds().getMinX(),
        (int)screen.getBounds().getMinY(),
        (int)screen.getBounds().getWidth(),
        (int)screen.getBounds().getHeight()
      );

      playerFrame.setBounds(bounds);
    }
  }
}
