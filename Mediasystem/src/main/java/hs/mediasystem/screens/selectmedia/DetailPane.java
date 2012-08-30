package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.util.AreaPane;
import hs.mediasystem.util.InheritanceDepthRanker;
import hs.mediasystem.util.PropertyClassEq;
import hs.mediasystem.util.ServiceTracker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.osgi.framework.BundleContext;

public abstract class DetailPane extends AreaPane {
  private final ServiceTracker<DetailPaneDecoratorFactory> detailPaneDecoratorFactoryTracker;

  private final ObjectProperty<Object> root = new SimpleObjectProperty<>();
  public ObjectProperty<Object> rootProperty() { return root; }

  public DetailPane(BundleContext bundleContext) {
    detailPaneDecoratorFactoryTracker = new ServiceTracker<>(bundleContext, DetailPaneDecoratorFactory.class, new InheritanceDepthRanker<DetailPaneDecoratorFactory>());

    root.addListener(new ChangeListener<Object>() {
      @Override
      public void changed(ObservableValue<? extends Object> observable, Object old, Object current) {
        System.out.println(">>> Attempting to get DetailPaneDecoratorFactory service for: " + current.getClass());
        DetailPaneDecoratorFactory factory = detailPaneDecoratorFactoryTracker.getService(new PropertyClassEq("mediasystem.class", current.getClass()));

        @SuppressWarnings("unchecked")
        DetailPaneDecorator<Object> decorator = (DetailPaneDecorator<Object>)factory.create();

        DetailPane.this.getChildren().clear();
        DetailPane.this.initialize();

        decorator.dataProperty().set(current);
        decorator.decorate(DetailPane.this);
      }
    });
  }

  /**
   * Set up the AreaPane with default groups.
   */
  protected abstract void initialize();
}
