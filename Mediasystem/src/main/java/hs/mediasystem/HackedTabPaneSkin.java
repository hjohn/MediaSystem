package hs.mediasystem;

import java.lang.reflect.Field;

import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.TabPaneSkin;

/**
 * Hack to get TabPane to work with just the Left/Right/Up/Down navigation keys:
 *
 * - For switching tabs it will use only the keys that make sense (left/right or up/down depending on orientation)
 * - When the first/last tab is active and navigation occurs to respectively the previous/next tab, then navigation is attempted to a neighbouring control
 * - Depending on the location of the tabs, the navigation keys will navigate to the tab's content (ie, when tabs are at the top, pressing down will go to the content)
 * - In combination with HackedTab class, it is possible to navigate from the inside of a tab to the outside if the cycle inside the tab is exhausted
 * - Focus is not required for navigation to work
 */
public class HackedTabPaneSkin extends TabPaneSkin {

  @SuppressWarnings("unchecked")
  public HackedTabPaneSkin(TabPane tabPane) {
    super(tabPane);

    try {
      Field behaviourField = SkinBase.class.getDeclaredField("behavior");
      Field keyEventListenerField = BehaviorBase.class.getDeclaredField("keyEventListener");

      behaviourField.setAccessible(true);
      keyEventListenerField.setAccessible(true);

      /*
       * Remove old behavior's key listener as otherwise all actions occur twice, then
       * install our behavior by overwriting the behavior field of SkinBase.
       */

      BehaviorBase<?> oldBehavior = (BehaviorBase<?>)behaviourField.get(this);

      tabPane.removeEventHandler(KeyEvent.ANY, (EventHandler<KeyEvent>)keyEventListenerField.get(oldBehavior));

      behaviourField.set(this, new TabPaneBehavior(tabPane));
    }
    catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static class TabPaneBehavior extends com.sun.javafx.scene.control.behavior.TabPaneBehavior {

    public TabPaneBehavior(TabPane paramTabPane) {
      super(paramTabPane);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void callAction(String direction) {
      Side side = getControl().getSide();
      boolean horizontal = side == Side.BOTTOM || side == Side.TOP;
      int selectedTabIndex = getControl().getSelectionModel().getSelectedIndex();
      int tabCount = getControl().getTabs().size();

      if(selectedTabIndex > 0 && (horizontal && "TraverseLeft".equals(direction) || !horizontal && "TraverseUp".equals(direction))) {
        selectPreviousTab();
      }
      else if(selectedTabIndex < tabCount - 1 && (horizontal && "TraverseRight".equals(direction) || !horizontal && "TraverseDown".equals(direction))) {
        selectNextTab();
      }
      else {
        TabPane localTabPane;
        if("TraverseNext".equals(direction) || side == Side.TOP && "TraverseDown".equals(direction) || side == Side.BOTTOM && "TraverseUp".equals(direction)
                                              || side == Side.LEFT && "TraverseLeft".equals(direction) || side == Side.RIGHT && "TraverseRight".equals(direction)) {
          localTabPane = getControl();
          TabPaneSkin localTabPaneSkin = (TabPaneSkin)localTabPane.getSkin();
          if(localTabPaneSkin.getSelectedTabContentRegion() != null) {
            localTabPaneSkin.getSelectedTabContentRegion().getImpl_traversalEngine().getTopLeftFocusableNode();
            if(localTabPaneSkin.getSelectedTabContentRegion().getImpl_traversalEngine().registeredNodes.isEmpty()) {
              Parent localParent = null;
              localParent = getFirstPopulatedInnerTraversalEngine(localTabPaneSkin.getSelectedTabContentRegion().getChildren());
              if(localParent != null) {
                int i = 0;
                for(Node localNode : localParent.getImpl_traversalEngine().registeredNodes) {
                  if(!localNode.isFocused() && localNode.impl_isTreeVisible() && !localNode.isDisabled()) {
                    localNode.requestFocus();
                    i = 1;
                    break;
                  }
                }
                if(i == 0) {
                  avoidCallingSuper(direction);
                }
              }
              else {
                avoidCallingSuper(direction);
              }
            }
          }
          else {
            avoidCallingSuper(direction);
          }
        }
        else if("Ctrl_Tab".equals(direction) || "Ctrl_Page_Down".equals(direction)) {
          localTabPane = getControl();
          if(localTabPane.getSelectionModel().getSelectedIndex() == localTabPane.getTabs().size() - 1) {
            localTabPane.getSelectionModel().selectFirst();
          }
          else {
            selectNextTab();
          }
          localTabPane.requestFocus();
        }
        else if("Ctrl_Shift_Tab".equals(direction) || "Ctrl_Page_Up".equals(direction)) {
          localTabPane = getControl();
          if(localTabPane.getSelectionModel().getSelectedIndex() == 0) {
            localTabPane.getSelectionModel().selectLast();
          }
          else {
            selectPreviousTab();
          }
          localTabPane.requestFocus();
        }
        else if("Home".equals(direction)) {
          if(getControl().isFocused()) {
            getControl().getSelectionModel().selectFirst();
          }
        }
        else if("End".equals(direction)) {
          if(getControl().isFocused()) {
            getControl().getSelectionModel().selectLast();
          }
        }
        else {
          avoidCallingSuper(direction);
        }
      }
    }

    private void avoidCallingSuper(String direction) {
      if("TraverseUp".equals(direction)) {
        traverseUp();
      }
      else if("TraverseDown".equals(direction)) {
        traverseDown();
      }
      else if("TraverseLeft".equals(direction)) {
        traverseLeft();
      }
      else if("TraverseRight".equals(direction)) {
        traverseRight();
      }
      else if("TraverseNext".equals(direction)) {
        traverseNext();
      }
      else if("TraversePrevious".equals(direction)) {
        traversePrevious();
      }
    }
  }
}
