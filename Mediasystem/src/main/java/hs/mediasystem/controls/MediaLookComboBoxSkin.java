package hs.mediasystem.controls;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import com.sun.javafx.scene.traversal.Direction;

/*
 * This class adds two level navigation to comboboxes.  It is currently the only way to change the
 * behavior of the relevant UI controls.
 */

public class MediaLookComboBoxSkin<T> extends ComboBoxListViewSkin<T> {

  public MediaLookComboBoxSkin(ComboBox<T> comboBox) {
    super(comboBox);

    EventDispatcher origEventDispatcher = comboBox.getEventDispatcher();

    comboBox.setEventDispatcher(new EventDispatcher() {
      @Override
      public Event dispatchEvent(Event event, EventDispatchChain tail) {
        if(event instanceof KeyEvent && !getSkinnable().isShowing()) {
          return tail.prepend(preemptiveEventDispatcher).dispatchEvent(event);
        }

        return origEventDispatcher.dispatchEvent(event, tail);
      }
    });
  }

  final EventDispatcher preemptiveEventDispatcher = new EventDispatcher() {
    @SuppressWarnings("deprecation")
    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {
      if(event instanceof KeyEvent && event.getEventType() == KeyEvent.KEY_PRESSED) {
        KeyEvent keyEvent = (KeyEvent)event;

        if(!keyEvent.isMetaDown() && !keyEvent.isControlDown() && !keyEvent.isAltDown()) {
          if(!getSkinnable().isShowing()) {
            Node node = (Node)event.getTarget();

            switch(keyEvent.getCode()) {
            case TAB:
              if(keyEvent.isShiftDown()) {
                node.impl_traverse(Direction.PREVIOUS);
              }
              else {
                node.impl_traverse(Direction.NEXT);
              }
              event.consume();
              break;
            case UP:
              node.impl_traverse(Direction.UP);
              event.consume();
              break;
            case DOWN:
              node.impl_traverse(Direction.DOWN);
              event.consume();
              break;
            case LEFT:
              node.impl_traverse(Direction.LEFT);
              event.consume();
              break;
            case RIGHT:
              node.impl_traverse(Direction.RIGHT);
              event.consume();
              break;
            case ENTER:
              getSkinnable().show();
              event.consume();
              break;
            default:
              Scene s = getSkinnable().getScene();
              Event.fireEvent(s, event);
              event.consume();
              break;
            }
          }
        }
      }

      return event;
    }
  };
}
