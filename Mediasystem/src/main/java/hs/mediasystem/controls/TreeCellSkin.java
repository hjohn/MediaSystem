package hs.mediasystem.controls;

import javafx.scene.control.TreeCell;

import com.sun.javafx.scene.control.behavior.TreeCellBehavior;
import com.sun.javafx.scene.control.skin.CellSkinBase;

// WORKAROUND for RT-28390: NPE in TreeCellSkin when TreeCell disclosureNode is set to null
public class TreeCellSkin<T> extends CellSkinBase<TreeCell<T>, TreeCellBehavior<T>> {

  public TreeCellSkin(TreeCell<T> treeCell) {
    super(treeCell, new TreeCellBehavior<>(treeCell));
  }
}
