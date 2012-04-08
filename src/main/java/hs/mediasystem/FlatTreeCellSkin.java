package hs.mediasystem;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import com.sun.javafx.scene.control.behavior.TreeCellBehavior;
import com.sun.javafx.scene.control.skin.CellSkinBase;

public class FlatTreeCellSkin extends CellSkinBase<TreeCell<?>, TreeCellBehavior> {

  public final double getIndent() {
    return 20;
  }

  public FlatTreeCellSkin(TreeCell<?> paramTreeCell) {
    super(paramTreeCell, new TreeCellBehavior(paramTreeCell));
  }

  @Override
  protected void layoutChildren() {
    TreeItem<?> localTreeItem = ((TreeCell<?>)getSkinnable()).getTreeItem();
    if(localTreeItem == null) {
      return;
    }

    TreeView<?> localTreeView = ((TreeCell<?>)getSkinnable()).getTreeView();
    if(localTreeView == null) {
      return;
    }

    double left = getInsets().getLeft();
    double top = getInsets().getTop();
    double width = getWidth() - (getInsets().getLeft() + getInsets().getRight());
    double height = getHeight() - (getInsets().getTop() + getInsets().getBottom());

    int i = TreeView.getNodeLevel(((TreeCell<?>)getSkinnable()).getTreeItem());
    if(!(localTreeView.isShowRoot())) {
      --i;
    }
    double indent = getIndent() * i;

    left += indent;

    int k = localTreeItem.getGraphic() == null ? 0 : 3;
    left += k;
    width -= indent + k;

    layoutLabelInArea(left, top, width, height);
  }
}