package hs.mediasystem.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.effect.PerspectiveTransform;
import javafx.scene.effect.Reflection;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class RayCarouselSkin<T> extends AbstractCarouselSkin<T> {

  public RayCarouselSkin(final TreeView<T> carousel) {
    super(carousel);

    InvalidationListener invalidationListener = new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        getSkinnable().requestLayout();
      }
    };

    cellAlignmentProperty().addListener(invalidationListener);
    reflectionEnabledProperty().addListener(invalidationListener);
    clipReflectionsProperty().addListener(invalidationListener);
    radiusRatioProperty().addListener(invalidationListener);
    viewDistanceRatioProperty().addListener(invalidationListener);
    maxCellWidthProperty().addListener(invalidationListener);
    maxCellHeightProperty().addListener(invalidationListener);
    viewAlignmentProperty().addListener(invalidationListener);
    carouselViewFractionProperty().addListener(invalidationListener);
  }

  private static class ReflectionConf {
    private final Reflection reflection;
    private final double distanceToReflectionTop;
    private final double reflectionHeight;

    public ReflectionConf(Reflection reflection, double distanceToReflectionTop, double reflectionHeight) {
      this.reflection = reflection;
      this.distanceToReflectionTop = distanceToReflectionTop;
      this.reflectionHeight = reflectionHeight;
    }

    public Reflection getReflection() {
      return reflection;
    }

    public double getDistanceToReflectionTop() {
      return distanceToReflectionTop;
    }

    public double getReflectionHeight() {
      return reflectionHeight;
    }
  }

  private class CellConfigurator {
    private final TreeCell<T> cell;
    private final double index;

    private ReflectionConf reflectionConf;

    private Point3D ul;
    private Point3D ur;
    private Point3D ll;
    private Point3D lr;
    private Point3D ulReflection;
    private Point3D urReflection;
    private Point2D ulReflection2d;
    private Point2D urReflection2d;

    private PerspectiveTransform perspectiveTransform;

    public CellConfigurator(TreeCell<T> cell, double index, ReflectionConf conf) {
      this.cell = cell;
      this.index = index;
      this.reflectionConf = conf;
    }

    private double getCarouselRadius() {
      return getSkinnable().getWidth() * getRadiusRatio();
    }

    public void calculateCarouselCoordinates() {
      Dimension2D cellSize = getCellSize2(cell);

      double angleOnCarousel = 2 * Math.PI * getCarouselViewFraction() / getInternalVisibleCellsCount() * index + Math.PI * 0.5;

      double carouselRadius = getCarouselRadius();
      double halfCellWidth = 0.5 * cellSize.getWidth();
      double h = cellSize.getHeight();
      double maxCellHeight = getMaxCellHeight();

      double cos = Math.cos(angleOnCarousel);
      double sin = -Math.sin(angleOnCarousel);

      double lx = (carouselRadius + halfCellWidth) * cos;
      double rx = (carouselRadius - halfCellWidth) * cos;
      double uy = -maxCellHeight * (1.0 - getViewAlignment()) + (maxCellHeight - h) * getCellAlignment();
      double ly = uy + h + (reflectionConf == null ? 0 : reflectionConf.getReflectionHeight() + reflectionConf.getDistanceToReflectionTop());
      double tz = (carouselRadius + halfCellWidth) * sin;
      double bz = (carouselRadius - halfCellWidth) * sin;

      ul = new Point3D(lx, uy, tz);
      ur = new Point3D(rx, uy, bz);
      ll = new Point3D(lx, ly, tz);
      lr = new Point3D(rx, ly, bz);

      if(reflectionConf != null) {
        ulReflection = new Point3D(lx, uy + h + reflectionConf.getDistanceToReflectionTop(), tz);
        urReflection = new Point3D(rx, uy + h + reflectionConf.getDistanceToReflectionTop(), bz);
      }
    }

    /**
     * Rotates the Cell towards the Viewer when it is close to the center.  Also mirrors
     * the cells after they passed the center to keep the Cells correctly visible for the
     * viewer.
     */
    public void applyViewRotation() {
      double cellsToRotate = 2;

      if(index < cellsToRotate) {
        double angle = index > -cellsToRotate ? Math.PI / 2 * -index / cellsToRotate + Math.PI / 2 : Math.PI;

        Point3D axis = new Point3D((ul.getX() + ur.getX()) / 2, 0, (ul.getZ() + ur.getZ()) / 2);

        ul = rotateY(ul, axis, angle);
        ur = rotateY(ur, axis, angle);
        ll = rotateY(ll, axis, angle);
        lr = rotateY(lr, axis, angle);

        if(reflectionConf != null) {
          ulReflection = rotateY(ulReflection, axis, angle);
          urReflection = rotateY(urReflection, axis, angle);
        }
      }
    }

    public Shape getReflectionClip() {
      if(reflectionConf == null || !getClipReflections()) {
        return null;
      }

      return new Polygon(
        ulReflection2d.getX(), ulReflection2d.getY(),
        urReflection2d.getX(), urReflection2d.getY(),
        perspectiveTransform.getLrx(), perspectiveTransform.getLry(),
        perspectiveTransform.getLlx(), perspectiveTransform.getLly()
      );
    }

    public PerspectiveTransform build() {
      double viewDistance = getViewDistanceRatio() * getCarouselRadius();
      double fov = (viewDistance - getCarouselRadius());

      // Z = -1 when normalized

      Point2D ul2d = project(ul, viewDistance, fov);
      Point2D ur2d = project(ur, viewDistance, fov);
      Point2D ll2d = project(ll, viewDistance, fov);
      Point2D lr2d = project(lr, viewDistance, fov);

      perspectiveTransform = new PerspectiveTransform(ul2d.getX(), ul2d.getY(), ur2d.getX(), ur2d.getY(), lr2d.getX(), lr2d.getY(), ll2d.getX(), ll2d.getY());

      if(reflectionConf != null) {
        ulReflection2d = project(ulReflection, viewDistance, fov);
        urReflection2d = project(urReflection, viewDistance, fov);

        perspectiveTransform.setInput(reflectionConf.getReflection());
      }

      return perspectiveTransform;
    }
  }

  @Override
  public Shape layoutCell(TreeCell<T> cell, double index) {
    ReflectionConf conf = null;

    if(getReflectionEnabled()) {
      conf = createReflection(cell);
    }

    CellConfigurator configurator = new CellConfigurator(cell, index, conf);

    configurator.calculateCarouselCoordinates();
    configurator.applyViewRotation();

    PerspectiveTransform perspectiveTransform = configurator.build();

    cell.setEffect(perspectiveTransform);

    return configurator.getReflectionClip();
  }

  private Point2D project(Point3D p, double viewDistance, double fov) {
    return new Point2D(snapPosition(p.getX() * fov / (p.getZ() + viewDistance)), snapPosition(p.getY() * fov / (p.getZ() + viewDistance)));
  }

  private static Point3D rotateY(Point3D p, Point3D axis, double radians) {
    Point3D input = new Point3D(p.getX() - axis.getX(), p.getY() - axis.getY(), p.getZ() - axis.getZ());

    return new Point3D(
      input.getZ() * Math.sin(radians) + input.getX() * Math.cos(radians) + axis.getX(),
      input.getY() + axis.getY(),
      input.getZ() * Math.cos(radians) - input.getX() * Math.sin(radians) + axis.getZ()
    );
  }

  private static final double REFLECTION_OPACITY = 0.5;

  public ReflectionConf createReflection(TreeCell<T> cell) {
    double reflectionMaxHeight = 50;

    double h = getCellSize2(cell).getHeight();
    double unusedHeight = getMaxCellHeight() - h;

    double horizonDistance = unusedHeight - unusedHeight * getCellAlignment();
    double reflectionPortion = (reflectionMaxHeight - horizonDistance) / h;
    double reflectionTopOpacity = REFLECTION_OPACITY - REFLECTION_OPACITY / reflectionMaxHeight * horizonDistance;
    double reflectionBottomOpacity = 0;

    if(reflectionPortion < 0 || reflectionTopOpacity < 0) {
      reflectionTopOpacity = 0;
      reflectionPortion = 0;
    }
    if(reflectionPortion > 1) {
      reflectionBottomOpacity = REFLECTION_OPACITY - REFLECTION_OPACITY / reflectionPortion;
      reflectionPortion = 1;
    }

    if(reflectionPortion > 0) {
      return new ReflectionConf(new Reflection(2 * horizonDistance / h * cell.prefHeight(-1), reflectionPortion, reflectionTopOpacity, reflectionBottomOpacity), 2 * horizonDistance, h * reflectionPortion);
    }

    return null;
  }
}
