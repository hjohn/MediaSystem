package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.util.AreaPane;
import hs.mediasystem.util.InheritanceDepthRanker;
import hs.mediasystem.util.PropertyClassEq;
import hs.mediasystem.util.ServiceTracker;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;

import org.osgi.framework.BundleContext;

public abstract class DetailPane extends StackPane {
  private final ServiceTracker<DetailPaneDecoratorFactory> detailPaneDecoratorFactoryTracker;

  private final ObjectProperty<Object> content = new SimpleObjectProperty<>();
  public ObjectProperty<Object> contentProperty() { return content; }

  private final DecoratablePane decoratablePane = new DecoratablePane(content);

  public DetailPane(BundleContext bundleContext) {
    getStylesheets().add("select-media/detail-pane.css");
    getStyleClass().add("detail-pane");

    detailPaneDecoratorFactoryTracker = new ServiceTracker<>(bundleContext, DetailPaneDecoratorFactory.class, new InheritanceDepthRanker<DetailPaneDecoratorFactory>());

    content.addListener(new ChangeListener<Object>() {
      @Override
      public void changed(ObservableValue<? extends Object> observable, Object old, Object current) {
        decoratablePane.decoratorContent.set(null);
      }
    });

    decoratablePane.finalContent.addListener(new ChangeListener<Object>() {
      @Override
      public void changed(ObservableValue<? extends Object> observable, Object old, Object current) {
        if(current != null) {
          DetailPaneDecoratorFactory factory = detailPaneDecoratorFactoryTracker.getService(new PropertyClassEq("mediasystem.class", current.getClass()));

          @SuppressWarnings("unchecked")
          DetailPaneDecorator<Object> decorator = (DetailPaneDecorator<Object>)factory.create(decoratablePane);

          decoratablePane.getStylesheets().clear();
          decoratablePane.getStyleClass().clear();
          decoratablePane.getChildren().clear();

          DetailPane.this.initialize(decoratablePane);

          decorator.dataProperty().set(current);
          decorator.decorate();
        }
      }
    });

    getChildren().add(decoratablePane);
  }

  /**
   * Set up the AreaPane with default groups.
   */
  protected abstract void initialize(DecoratablePane decoratablePane);

  public static class DecoratablePane extends AreaPane {
    private final ObjectProperty<Object> decoratorContent = new SimpleObjectProperty<>();
    public ObjectProperty<Object> decoratorContentProperty() { return decoratorContent; }

    private final ObjectBinding<Object> finalContent;
    public ObjectBinding<Object> finalContentBinding() { return finalContent; }

    public DecoratablePane(ObjectProperty<Object> content) {
      this.finalContent = Bindings.when(decoratorContent.isNull()).then(content).otherwise(decoratorContent);
    }
  }
}
