package hs.mediasystem.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public abstract class AbstractCarouselSkin<T> extends TreeViewSkin<T> {
  private final DoubleProperty cellAlignment = new SimpleDoubleProperty(0.8);
  public final DoubleProperty cellAlignmentProperty() { return cellAlignment; }
  public final double getCellAlignment() { return cellAlignment.get(); }

  private final BooleanProperty reflectionEnabled = new SimpleBooleanProperty(true);
  public final BooleanProperty reflectionEnabledProperty() { return reflectionEnabled; }
  public final boolean getReflectionEnabled() { return reflectionEnabled.get(); }

  private final BooleanProperty clipReflections = new SimpleBooleanProperty(true);
  public final BooleanProperty clipReflectionsProperty() { return clipReflections; }
  public final boolean getClipReflections() { return clipReflections.get(); }

  private final DoubleProperty radiusRatio = new SimpleDoubleProperty(1.0);
  public final DoubleProperty radiusRatioProperty() { return radiusRatio; }
  public final double getRadiusRatio() { return radiusRatio.get(); }

  private final DoubleProperty viewDistanceRatio = new SimpleDoubleProperty(2.0);
  public final DoubleProperty viewDistanceRatioProperty() { return viewDistanceRatio; }
  public final double getViewDistanceRatio() { return viewDistanceRatio.get(); }

  private final DoubleProperty viewAlignment = new SimpleDoubleProperty(0.5);
  public final DoubleProperty viewAlignmentProperty() { return viewAlignment; }
  public final double getViewAlignment() { return viewAlignment.get(); }

  private final DoubleProperty carouselViewFraction = new SimpleDoubleProperty(0.5);
  public final DoubleProperty carouselViewFractionProperty() { return carouselViewFraction; }
  public final double getCarouselViewFraction() { return carouselViewFraction.get(); }

  private final DoubleProperty density = new SimpleDoubleProperty(0.02);
  public final DoubleProperty densityProperty() { return density; }
  public final double getDensity() { return density.get(); }

  private final DoubleProperty maxCellWidth = new SimpleDoubleProperty(300);
  public final DoubleProperty maxCellWidthProperty() { return maxCellWidth; }
  public final double getMaxCellWidth() { return maxCellWidth.get(); }

  private final DoubleProperty maxCellHeight = new SimpleDoubleProperty(200);
  public final DoubleProperty maxCellHeightProperty() { return maxCellHeight; }
  public final double getMaxCellHeight() { return maxCellHeight.get(); }

  private final ArrayList<TreeCell<T>> cells = new ArrayList<>();

  private double internalVisibleCellsCount;

  protected double getInternalVisibleCellsCount() {
    return internalVisibleCellsCount;
  }


  private final Transition transition = new Transition() {
    {
      setCycleDuration(Duration.millis(500));
    }

    @Override
    protected void interpolate(double frac) {
      fractionalIndex = startFractionalIndex - startFractionalIndex * frac;

      sortChildren();
      getSkinnable().requestLayout();
    }
  };

  private void sortChildren() {

    /*
     * Update the cell indices.
     */

    int index = getSkinnable().getFocusModel().getFocusedIndex() - (int)Math.round(fractionalIndex);
    int visibleCellsCount = cells.size();
    int start = index - (visibleCellsCount - 1) / 2;
    int end = index + visibleCellsCount / 2;

    double opacity = ((fractionalIndex > 0 ? fractionalIndex : 1 + fractionalIndex % 1) + 0.5) % 1;

    for(int i = start; i <= end; i++) {
      TreeCell<T> carouselCell = cells.get((i + visibleCellsCount) % visibleCellsCount);

      carouselCell.updateIndex(i);

      if(i == start) {
        carouselCell.setOpacity(opacity);
      }
      else if(i == end) {
        carouselCell.setOpacity(1.0 - opacity);
      }
      else {
        carouselCell.setOpacity(1.0);
      }
    }

    /*
     * Resort the children of the StackPane so the cells closest to center are on top.  A temporary list
     * is used to prevent events firing (and to avoid duplicate items in the Scene caused by the sorting steps).
     */
    // TODO perhaps this is possible to achieve with toFront() ?

    List<Node> temporaryList = new ArrayList<>(getChildren());
    Collections.sort(temporaryList, Z_ORDER_FRAC);
    getChildren().setAll(temporaryList);
  }

  private double startFractionalIndex;
  private double fractionalIndex;

  public AbstractCarouselSkin(final TreeView<T> carousel) {
    super(carousel);

    getSkinnable().getStyleClass().add("carousel");

    InvalidationListener cellCountInvalidationListener = new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        allocateCells();
        sortChildren();
      }
    };

    carousel.widthProperty().addListener(cellCountInvalidationListener);
    densityProperty().addListener(cellCountInvalidationListener);

    allocateCells();

    carousel.getFocusModel().focusedIndexProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number old, Number current) {

        /*
         * Calculate at how many (fractional) items distance from the middle the carousel currently is and start the transition that will
         * move the now focused cell to the middle.
         */

        startFractionalIndex = fractionalIndex - old.doubleValue() + current.doubleValue();
        transition.playFromStart();
      }
    });
  }

  // Goal: Spacings between Cells should remain similar when width changes
  private void allocateCells() {
    double widthFactor = getDensity();

    internalVisibleCellsCount = getSkinnable().getWidth() * widthFactor;
    internalVisibleCellsCount = internalVisibleCellsCount < 3 ? 3 : internalVisibleCellsCount;

    int preferredCellCount = (int)internalVisibleCellsCount;

    if(cells.size() > preferredCellCount) {
      List<TreeCell<T>> cellsToBeDeleted = cells.subList(preferredCellCount, cells.size());

      for(TreeCell<T> carouselCell : cellsToBeDeleted) {
        carouselCell.updateIndex(-1);
      }

      getChildren().removeAll(cellsToBeDeleted);
      cellsToBeDeleted.clear();
    }
    else if(cells.size() < preferredCellCount) {
      for(int i = cells.size(); i < preferredCellCount; i++) {
        TreeCell<T> cell = createCell();

        cell.updateTreeView(getSkinnable());
        cell.updateIndex(i);

        cells.add(cell);
        getChildren().add(cell);
      }
    }
  }

  private final Comparator<Node> Z_ORDER_FRAC = new Comparator<Node>() {
    @Override
    public int compare(Node o1, Node o2) {
      TreeCell<?> cell1 = (TreeCell<?>)o1;
      TreeCell<?> cell2 = (TreeCell<?>)o2;

      int selectedIndex = getSkinnable().getFocusModel().getFocusedIndex();

      int dist1 = Math.abs(selectedIndex - cell1.getIndex() - (int)Math.round(fractionalIndex));
      int dist2 = Math.abs(selectedIndex - cell2.getIndex() - (int)Math.round(fractionalIndex));

      return Integer.compare(dist2, dist1);
    }
  };

//  @Override
//  private TreeCell<T> createCell() {
//    Callback<Carousel<T>, CarouselCell<T>> cellFactory = getSkinnable().getCellFactory();
//
//    if(cellFactory == null) {
//      return new CarouselCell<T>() {
//        @Override
//        protected void updateItem(T item, boolean empty) {
//          super.updateItem(item, empty);
//
//          if(!empty) {
//            setText(item.toString());
//          }
//        }
//      };
//    }
//
//    return cellFactory.call(getSkinnable());
//  }

//  @Override
//  protected double computeMinWidth(double height) {
//    return 16;
//  }
//
//  @Override
//  protected double computeMinHeight(double width) {
//    return 16;
//  }
//
//  @Override
//  protected double computePrefWidth(double height) {
//    return 16;
//  }
//
//  @Override
//  protected double computePrefHeight(double width) {
//    return 16;
//  }

//  @Override
//  protected void layoutChildren() {
//    doLayout();
//  }
  protected double getCellAspectRatio() {
    return getMaxCellWidth() / getMaxCellHeight();
  }

  public Dimension2D getCellSize2(TreeCell<T> cell) {
    double prefWidth = cell.prefWidth(-1);
    double prefHeight = cell.prefHeight(-1);

    if(prefWidth > getMaxCellWidth()) {
      prefHeight = prefHeight / prefWidth * getMaxCellWidth();
      prefWidth = getMaxCellWidth();
    }
    if(prefHeight > getMaxCellHeight()) {
      prefWidth = prefWidth / prefHeight * getMaxCellHeight();
      prefHeight = getMaxCellHeight();
    }

    return new Dimension2D(prefWidth, prefHeight);
  }

  public Dimension2D getCellSize(TreeCell<T> cell) {
    double prefWidth = cell.prefWidth(-1);
    double prefHeight = cell.prefHeight(-1);

    return new Dimension2D(prefWidth, prefHeight);
  }

  private void doLayout(double w, double h) {
    Shape cumulativeClip = null;
    int selectedIndex = getSkinnable().getFocusModel().getFocusedIndex();

    /*
     * Positions the Cells in front-to-back order.  This is done in order to clip the reflections
     * of cells positioned behind other cells using a cumulative clip.  Reflections would otherwise
     * blend with each other as they are partially transparent in nature.
     */

    ListIterator<Node> iterator = getChildren().listIterator(getChildren().size());

    while(iterator.hasPrevious()) {
      @SuppressWarnings("unchecked")
      TreeCell<T> cell = (TreeCell<T>)iterator.previous();

      cell.setVisible(!cell.isEmpty());

      if(!cell.isEmpty()) {
        Shape clip = layoutCell(cell, selectedIndex - cell.getIndex() - fractionalIndex);

        Dimension2D cellSize = getCellSize(cell);
        System.out.println(">>> celllsize = " + cellSize + " min: " + cell.minWidth(-1) + "x" + cell.minHeight(-1) + "; pref: " + cell.prefWidth(-1) + "x" + cell.prefHeight(-1) + "; max: " + cell.maxWidth(-1) + "x" + cell.maxHeight(-1));
        layoutInArea(cell, w / 2, h / 2, cellSize.getWidth(), cellSize.getHeight(), 0, HPos.CENTER, VPos.CENTER);
//        layoutInArea(cell, w / 2, h / 2, 0, 0, 0, HPos.CENTER, VPos.CENTER);

  //      System.out.println(">>> layoutInArea: " + w/2 + ": " + h/2 + " index=" + (selectedIndex - cell.getIndex() - fractionalIndex) + ": " + cell);

        if(cumulativeClip != null) {
          Shape cellClip = Shape.intersect(cumulativeClip, new Rectangle(0, 0, w, h));  // TODO there must be a better way to just copy a Shape...
          Point2D localToParent = cell.localToParent(0, 0);

          cellClip.getTransforms().add(new Translate(-localToParent.getX(), -localToParent.getY()));

          cell.setClip(cellClip);
        }
        else {
          cell.setClip(null);
        }

        if(clip != null) {
          clip.getTransforms().add(cell.getLocalToParentTransform());

          if(cumulativeClip == null) {
            cumulativeClip = new Rectangle(0, 0, w, h);
          }

          cumulativeClip = Shape.subtract(cumulativeClip, clip);  // TODO a copy is made here...
        }
      }
    }

//    setClip(new Rectangle(0, 0, getWidth(), getHeight()));
  }

  // index = fractional index
  public abstract Shape layoutCell(TreeCell<T> cell, double index);







  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    System.out.println(">>> layoutChildren: " + x + ", " + y + ", w=" + w + ", h=" + h);
//    markAllCellsUnused();
//    getChildren().clear();

    getSkinnable().setClip(new Rectangle(x, y, w, h));

    doLayout(w, h);

//    discardUnusedCells();
  }
}
