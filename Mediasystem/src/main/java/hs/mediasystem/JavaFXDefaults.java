package hs.mediasystem;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;
import javafx.scene.control.Button;

import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.scene.control.behavior.KeyBinding;

public class JavaFXDefaults {
  private static final String PRESS_ACTION = "Press";
  private static final String RELEASE_ACTION = "Release";

  /**
   * Changes various settings in JavaFX controls.
   *
   * Changes are made for:
   *
   * Allowing global hotkeys for common navigiation keys; arrow keys are used for navigation but also for player skipping; navigation
   * should take priority if applicable but it is handled currently only when an event is not consumed by anything else.  Since hotkeys
   * will consume events too early, TRAVERSAL_BINDINGS are added explicitly to some controls to have navigation consume these first.
   *   - ButtonBehavior
   *
   * Making ENTER work for all controls as a selection key.
   *   - ButtonBehavior (normally only uses SPACE)
   */
  @SuppressWarnings("unused")
  public static void setup() {
    new ButtonBehavior<Button>(new Button()) {{
      BUTTON_BINDINGS.add(new KeyBinding(ENTER, KEY_PRESSED, PRESS_ACTION));
      BUTTON_BINDINGS.add(new KeyBinding(ENTER, KEY_RELEASED, RELEASE_ACTION));
      BUTTON_BINDINGS.addAll(TRAVERSAL_BINDINGS);
    }};
  }
}
