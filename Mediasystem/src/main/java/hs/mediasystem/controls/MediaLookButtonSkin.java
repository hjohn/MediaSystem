package hs.mediasystem.controls;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;

import com.sun.javafx.scene.control.skin.LabeledSkinBase;

/*
 * This class is a minimal reimplementation of the likely named class of the JDK as it is the only way to change the
 * behavior of the relevant UI controls.
 */

public class MediaLookButtonSkin extends LabeledSkinBase<Button, MediaLookButtonBehavior<Button>> {

  public MediaLookButtonSkin(Button button) {
    super(button, new MediaLookButtonBehavior<>(button));

    registerChangeListener(button.focusedProperty(), "FOCUSED");
  }

  @Override
  protected void handleControlPropertyChanged(String p) {
    super.handleControlPropertyChanged(p);

    if("FOCUSED".equals(p)) {
      if(!getSkinnable().isFocused()) {
        ContextMenu cm = getSkinnable().getContextMenu();
        if(cm != null) {
          if(cm.isShowing()) {
            cm.hide();
            //  Utils.removeMnemonics(cm, getSkinnable().getScene());
          }
        }
      }
    }
  }
}
