package hs.mediasystem.util.javafx;

import java.lang.reflect.Field;

import javafx.scene.control.Control;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ListViewBehavior;
import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusBehavior;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class TwoLevelFocus {

  public static void setInternalFocus(Control control) {
    if(control.getSkin() != null) {
      BehaviorBase<?> behavior = ((BehaviorSkinBase<?,?>)control.getSkin()).getBehavior();

      if(behavior instanceof ListViewBehavior || behavior instanceof TableViewBehavior) {
        try {
          Field field = behavior.getClass().getDeclaredField("tlFocus");

          field.setAccessible(true);
          TwoLevelFocusBehavior twoLevelFocusBehavior = (TwoLevelFocusBehavior)field.get(behavior);

          if(twoLevelFocusBehavior != null) {
            twoLevelFocusBehavior.setExternalFocus(false);
          }
        }
        catch(Exception e) {
          throw new IllegalStateException(e);
        }
      }
    }
  }
}
