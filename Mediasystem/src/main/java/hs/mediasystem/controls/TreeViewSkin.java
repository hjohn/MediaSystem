package hs.mediasystem.controls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

import com.sun.javafx.scene.control.behavior.TreeViewBehavior;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/*
 * Paging problem
 * ==============
 * When paging to a new page using pg-up/down it is very hard to determine which cell
 * should be focused on the new page.  When paging down for example, the newly focused
 * cell should be the one that is at the bottom of the page, while the cell that
 * previously had the focus should be at the top of the page.
 *
 * The problem occurs because to know which cell will end up at the bottom of the page
 * is unknown due to cell heights not being known for the newly visible cells.  If
 * their heights vary and are not known immediately (due to background loading of
 * data) the problem in fact is not solvable.
 *
 * There are several situations we can distinguish:
 *
 * 1) Cell heights are final (fixed height or not).  The newly focused cell can be
 *    found by querying the heights of all cells starting from the new top cell to the
 *    first cell found to not fit in the view.
 *
 * 2) Cell heights are not final; the final value can only be determined after fully
 *    loading the cell.  This problem is not solvable.  A possible solution is to
 *    change the pg-down/up functionality for this case.  Possibilities could be:
 *
 *    a) Scroll a fixed, selectable number of cells (like 5).
 *
 *    b) Scroll a percentage of the currently visible cells, in the hope that the next
 *       page will roughly have the same cell height distribution.
 *
 *    c) Move the current focused cell to the top, but instead of focusing the new
 *       bottom cell leave the top one focused until the user pages again.  This means
 *       "paging" effectively requires two pg-down/up pressed, but it gives a bit of
 *       time to let the new page load.
 *
 *    Of these options only the two-step paging potentially can avoid skipping over
 *    cells.  The other methods can avoid it as well in most practical cases, where
 *    practical means that the control shows a reasonable number of visible cells to
 *    make a View control a practical control for displaying them (a View control that
 *    shows say less than 5 cells in certain situations is not very practical to
 *    navigate with pg-up/down.
 */


public class TreeViewSkin<T> extends BehaviorSkinBase<TreeView<T>, TreeViewBehavior<T>>  {
  private final Set<TreeCell<T>> usedCells = new HashSet<>();
  private final List<TreeCell<T>> reusableCells = new ArrayList<>();

  private int maximumCells = 100;

  private TreeCell<T> firstFullyVisibleCell;
  private TreeCell<T> lastFullyVisibleCell;
  private double targetCellOffset = 0.5;

  public TreeViewSkin(TreeView<T> treeView) {
    super(treeView, new TreeViewBehavior<T>(treeView));

//    getSkinnable().addEventHandler(ScrollToEvent.SCROLL_TO_TOP_INDEX, new EventHandler<ScrollToEvent<Integer>>() {
//      @Override
//      public void handle(ScrollToEvent<Integer> event) {
//        targetCellOffset = 0.5;
//      }
//    });

    getBehavior().setOnFocusPreviousRow(new Runnable() {
        @Override public void run() { }
    });
    getBehavior().setOnFocusNextRow(new Runnable() {
        @Override public void run() { }
    });
    getBehavior().setOnMoveToFirstCell(new Runnable() {
        @Override public void run() { }
    });
    getBehavior().setOnMoveToLastCell(new Runnable() {
        @Override public void run() { }
    });
    getBehavior().setOnScrollPageDown(new Callback<Integer, Integer>() {
        @Override
        public Integer call(Integer anchor) {
          if(lastFullyVisibleCell.isFocused()) {
            targetCellOffset = 0;
            getSkinnable().requestLayout();
          }

          return lastFullyVisibleCell.getIndex();
        }
    });
    getBehavior().setOnScrollPageUp(new Callback<Integer, Integer>() {
        @Override
        public Integer call(Integer anchor) {
          if(firstFullyVisibleCell.isFocused()) {
            targetCellOffset = 1;
            getSkinnable().requestLayout();
          }

          return firstFullyVisibleCell.getIndex();
        }
    });
    getBehavior().setOnSelectPreviousRow(new Runnable() {
        @Override public void run() { }
    });
    getBehavior().setOnSelectNextRow(new Runnable() {
        @Override public void run() { }
    });

    getSkinnable().getFocusModel().focusedItemProperty().addListener(new ChangeListener<TreeItem<T>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<T>> observableValue, TreeItem<T> old, TreeItem<T> current) {
        System.out.println(">>> focus changed from " + old + " to " + current);

        if(current != null) {
          TreeCell<T> treeCell = getTreeCell(current);
          int index = calculateIndex(current);

          if(index < firstFullyVisibleCell.getIndex()) {
            targetCellOffset = 0;
            getSkinnable().requestLayout();
          }
          else if(index > lastFullyVisibleCell.getIndex()) {
            targetCellOffset = 1;
            getSkinnable().requestLayout();
          }
          else {
            targetCellOffset = computeTargetCellOffset(treeCell);
          }

          System.out.println(">>> targetCellOffset = " + targetCellOffset + "; index = " + index + "; lastFullyVisibleCell = " + lastFullyVisibleCell.getIndex() + " -> " + lastFullyVisibleCell.getTreeItem());
        }
      }
    });
  }


  private double computeTargetCellOffset(TreeCell<T> cell) {
    double cellCenter = cell.getLayoutY() - getSkinnable().getInsets().getTop() + cell.getHeight() / 2;

    System.out.println(">>> layoutY = " + cell.getLayoutY() + "; cell.height = " + cell.getHeight() + "; tree.height = " + getSkinnable().getHeight() + "; insets = " + getSkinnable().getInsets());

    return cellCenter / (getSkinnable().getHeight() - getSkinnable().getInsets().getTop() - getSkinnable().getInsets().getBottom());
  }

  private final Map<TreeItem<T>, TreeCell<T>> treeCells = new HashMap<>();

  private TreeCell<T> getTreeCell(TreeItem<T> item) {
    TreeCell<T> cell = treeCells.get(item);

    if(cell == null) {
      if(reusableCells.isEmpty()) {
        cell = createCell();
      }
      else {
        cell = reusableCells.remove(reusableCells.size() - 1);
      }

      cell.updateTreeItem(item);
      treeCells.put(item, cell);
    }

    usedCells.add(cell);

    return cell;
  }

  private static <T> TreeItem<T> lastVisibleLeaf(TreeItem<T> item) {
    if(!item.isExpanded()) {
      return item;
    }

    ObservableList<TreeItem<T>> children = item.getChildren();

    if(children.isEmpty()) {
      return item;
    }

    return lastVisibleLeaf(children.get(children.size() - 1));
  }

  private TreeItem<T> previous(TreeItem<T> item) {
    TreeItem<T> parent = item.getParent();

    if(parent == null) {
      return null;
    }

    int index = parent.getChildren().indexOf(item);

    if(index == 0) {
      if(parent.getParent() == null && !getSkinnable().isShowRoot()) {
        return null;
      }

      return parent;
    }

    return lastVisibleLeaf(parent.getChildren().get(index - 1));
  }

  private static <T> TreeItem<T> next(TreeItem<T> item) {
    if(item.isExpanded() && !item.getChildren().isEmpty()) {
      return item.getChildren().get(0);
    }

    TreeItem<T> current = item;

    for(;;) {
      TreeItem<T> parent = current.getParent();

      if(parent == null) {
        return null;
      }

      ObservableList<TreeItem<T>> children = parent.getChildren();
      int index = children.indexOf(current) + 1;

      if(index < children.size()) {
        return children.get(index);
      }

      current = parent;
    }
  }

  private int calculateIndex(TreeItem<T> item) {
    TreeItem<T> current = item;
    int index = 0;

    while((current = previous(current)) != null) {
      index++;
    }

    return index;
  }

//  @Override
//  protected double computePrefHeight(double width) {
//    return 20;
//  }

  private void markAllCellsUnused() {
    usedCells.clear();
  }

  private void discardUnusedCells() {
    Iterator<TreeCell<T>> iterator = treeCells.values().iterator();

    while(iterator.hasNext()) {
      TreeCell<T> cell = iterator.next();

      if(!usedCells.contains(cell)) {
        iterator.remove();
        cell.updateIndex(-1);  // TODO do more here to empty cell, updateItem, updateTreeItem, etc.
        reusableCells.add(cell);
      }
    }

    if(reusableCells.size() >= maximumCells) {
      reusableCells.subList(maximumCells, reusableCells.size()).clear();
    }
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    markAllCellsUnused();
    getChildren().clear();

    getSkinnable().setClip(new Rectangle(x, y, w, h));

    TreeItem<T> focusedItem = getSkinnable().getFocusModel().getFocusedItem();

    if(focusedItem == null) {
      focusedItem = getSkinnable().getRoot();
    }

    TreeCell<T> targetTreeCell = getTreeCell(focusedItem);

    getChildren().add(targetTreeCell);

    double cellHeight = targetTreeCell.prefHeight(-1);
    double startY = snapPosition(h * targetCellOffset - cellHeight / 2);

    if(startY + cellHeight > h) {
      startY = h - cellHeight;
    }
    if(startY < 0) {
      startY = 0;
    }

    TreeItem<T> firstItem = focusedItem;

    System.out.println(">>> Laying out, startY = " + startY + " cellHeight = " + cellHeight + ", focusedItem = " + focusedItem);

    while(startY > 0) {
      TreeItem<T> previous = previous(firstItem);

      if(previous == null) {
        break;
      }

      firstItem = previous;

      TreeCell<T> treeCell = getTreeCell(firstItem);

      getChildren().add(treeCell);

      startY -= treeCell.prefHeight(-1);
    }

    int index = calculateIndex(firstItem);

    System.out.println(">>> Laying out " + x + "; " + y + "; " + w + "x" + h + ", startIndex = " + index + ", firstItem = " + firstItem + ", focusedItem = " + focusedItem);

    if(startY > 0) {
      startY = 0;
    }

    /*
     * firstCell = first cell to draw
     * startY = y position of first cell to draw
     */

    startY += y;

    firstFullyVisibleCell = null;

    while(firstItem != null && startY < h) {
      TreeCell<T> cell = getTreeCell(firstItem);

      if(!getChildren().contains(cell)) {
        getChildren().add(cell);
      }

      cell.updateIndex(index++);
      double ch = cell.prefHeight(-1);
      cell.resizeRelocate(x, startY, w, ch);

      if(startY >= 0 && firstFullyVisibleCell == null) {
        firstFullyVisibleCell = cell;
      }
      if(startY - y + ch <= h) {
        lastFullyVisibleCell = cell;
      }

      startY += ch;

      firstItem = next(firstItem);
    }

    discardUnusedCells();
  }

  public TreeCell<T> createCell() {
    TreeCell<T> cell;
    if(getSkinnable().getCellFactory() != null) {
      cell = getSkinnable().getCellFactory().call(getSkinnable());
    }
    else {
      cell = createDefaultCellImpl();
    }

    cell.updateTreeView(getSkinnable());

    return cell;
  }

  private TreeCell<T> createDefaultCellImpl() {
    return new TreeCell<T>() {
        private HBox hbox;

        @Override public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                hbox = null;
                setText(null);
                setGraphic(null);
            } else {
                // update the graphic if one is set in the TreeItem
                TreeItem<?> treeItem = getTreeItem();
                if (treeItem != null && treeItem.getGraphic() != null) {
                    if (item instanceof Node) {
                        setText(null);

                        // the item is a Node, and the graphic exists, so
                        // we must insert both into an HBox and present that
                        // to the user (see RT-15910)
                        if (hbox == null) {
                            hbox = new HBox(3);
                        }
                        hbox.getChildren().setAll(treeItem.getGraphic(), (Node)item);
                        setGraphic(hbox);
                    } else {
                        hbox = null;
                        setText(item.toString());
                        setGraphic(treeItem.getGraphic());
                    }
                } else {
                    hbox = null;
                    if (item instanceof Node) {
                        setText(null);
                        setGraphic((Node)item);
                    } else {
                        setText(item.toString());
                        setGraphic(null);
                    }
                }
            }
        }
    };
  }
}
