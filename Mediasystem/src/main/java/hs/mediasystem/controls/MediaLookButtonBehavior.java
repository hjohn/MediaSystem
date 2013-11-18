package hs.mediasystem.controls;

import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

/*
 * This class is a minimal reimplementation of the likely named class of the JDK as it is the only way to change the
 * behavior of the relevant UI controls.
 */

public class MediaLookButtonBehavior<C extends ButtonBase> extends BehaviorBase<C> {

  public MediaLookButtonBehavior(final C button) {
    super(button, BUTTON_BINDINGS);
  }

  @Override
  protected void focusChanged() {
    final ButtonBase button = getControl();
    if(keyDown && !button.isFocused()) {
      keyDown = false;
      button.disarm();
    }
  }

  private boolean keyDown;

  private static final String PRESS_ACTION = "Press";
  private static final String RELEASE_ACTION = "Release";

  protected static final List<KeyBinding> BUTTON_BINDINGS = new ArrayList<>();

  static {
    BUTTON_BINDINGS.add(new KeyBinding(ENTER, KEY_PRESSED, PRESS_ACTION));
    BUTTON_BINDINGS.add(new KeyBinding(ENTER, KEY_RELEASED, RELEASE_ACTION));
    BUTTON_BINDINGS.add(new KeyBinding(SPACE, KEY_PRESSED, PRESS_ACTION));
    BUTTON_BINDINGS.add(new KeyBinding(SPACE, KEY_RELEASED, RELEASE_ACTION));
  }

  @Override
  protected void callAction(String name) {
    if(!getControl().isDisabled()) {
      if(PRESS_ACTION.equals(name)) {
        keyPressed();
      }
      else if(RELEASE_ACTION.equals(name)) {
        keyReleased();
      }
      else {
        super.callAction(name);
      }
    }
  }

  private void keyPressed() {
    final ButtonBase button = getControl();
    if(!button.isPressed() && !button.isArmed()) {
      keyDown = true;
      button.arm();
    }
  }

  private void keyReleased() {
    final ButtonBase button = getControl();
    if(keyDown) {
      keyDown = false;
      if(button.isArmed()) {
        button.disarm();
        button.fire();
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    final ButtonBase button = getControl();
    super.mousePressed(e);

    if(!button.isFocused() && button.isFocusTraversable()) {
      button.requestFocus();
    }

    boolean valid = (e.getButton() == MouseButton.PRIMARY && !(e.isMiddleButtonDown() || e.isSecondaryButtonDown() || e.isShiftDown() || e.isControlDown() || e.isAltDown() || e.isMetaDown()));

    if(!button.isArmed() && valid) {
      button.arm();
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    final ButtonBase button = getControl();
    if(!keyDown && button.isArmed()) {
      button.fire();
      button.disarm();
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    final ButtonBase button = getControl();
    super.mouseEntered(e);
    if(!keyDown && button.isPressed()) {
      button.arm();
    }
  }

  @Override
  public void mouseExited(MouseEvent e) {
    final ButtonBase button = getControl();
    super.mouseExited(e);
    if(!keyDown && button.isArmed()) {
      button.disarm();
    }
  }
}
