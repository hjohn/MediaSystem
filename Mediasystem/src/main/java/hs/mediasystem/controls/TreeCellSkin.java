package hs.mediasystem.controls;

import javafx.scene.control.TreeCell;

import com.sun.javafx.scene.control.behavior.TreeCellBehavior;
import com.sun.javafx.scene.control.skin.CellSkinBase;

public class TreeCellSkin extends CellSkinBase<TreeCell<?>, TreeCellBehavior> {

  public TreeCellSkin(TreeCell<?> treeCell) {
    super(treeCell, new TreeCellBehavior(treeCell));
  }
}
