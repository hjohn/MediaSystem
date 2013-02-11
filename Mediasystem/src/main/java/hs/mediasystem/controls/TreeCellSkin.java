package hs.mediasystem.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.TreeCell;

import com.sun.javafx.css.CssMetaData;
import com.sun.javafx.scene.control.behavior.TreeCellBehavior;
import com.sun.javafx.scene.control.skin.CellSkinBase;

public class TreeCellSkin extends CellSkinBase<TreeCell<?>, TreeCellBehavior> {

  public TreeCellSkin(TreeCell<?> treeCell) {
    super(treeCell, new TreeCellBehavior(treeCell));

    //super(treeCell, new TreeCellBehavior(treeCell));

  }


  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
//    System.out.println(">>> laying out cell: " + x + "," + y+ " :: " + w + "x"+ h);
 //   layoutInArea(getSkinnable(), x, y, w, h, 0, null, true, true, HPos.CENTER, VPos.CENTER);
    layoutLabelInArea(x, y, w, h);
  }

  /***************************************************************************
   *                                                                         *
   *                         Stylesheet Handling                             *
   *                                                                         *
   **************************************************************************/

  /** @treatAsPrivate */
  @SuppressWarnings("rawtypes")
  private static class StyleableProperties {
      private static final List<CssMetaData> STYLEABLES;

      static {
          final List<CssMetaData> styleables = new ArrayList<>(CellSkinBase.getClassCssMetaData());

          STYLEABLES = Collections.unmodifiableList(styleables);
      }
  }

  /**
   * @return The CssMetaData associated with this class, which may include the
   * CssMetaData of its super classes.
   */
  @SuppressWarnings("rawtypes")
  public static List<CssMetaData> getClassCssMetaData() {
      return StyleableProperties.STYLEABLES;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("rawtypes")
  public List<CssMetaData> getCssMetaData() {
      return getClassCssMetaData();
  }
}
