package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.util.AreaPane;

import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;

public abstract class DetailPane extends StackPane {
 // private final ServiceTracker<DetailPaneDecoratorFactory> detailPaneDecoratorFactoryTracker;

  private final ObjectProperty<Object> content = new SimpleObjectProperty<>();
  public ObjectProperty<Object> contentProperty() { return content; }

  private final DecoratablePane decoratablePane = new DecoratablePane(content);
  private final Set<DetailPaneDecoratorFactory> detailPaneDecoratorFactories;

  public DetailPane(Set<DetailPaneDecoratorFactory> detailPaneDecoratorFactories, final boolean interactive) {
    System.out.println(">>> Created DetailPane with " + detailPaneDecoratorFactories.size() + ": " + detailPaneDecoratorFactories);

    this.detailPaneDecoratorFactories = detailPaneDecoratorFactories;
    getStylesheets().add("select-media/detail-pane.css");
    getStyleClass().add("detail-pane");

    setMouseTransparent(!interactive);

   // detailPaneDecoratorFactoryTracker = new ServiceTracker<>(bundleContext, DetailPaneDecoratorFactory.class, new InheritanceDepthRanker<DetailPaneDecoratorFactory>());

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
          DetailPaneDecoratorFactory factory = findDetailPaneDecoratorFactory(current.getClass());
System.out.println(">>> Chosen " + factory.getType() + ", " + factory);
          @SuppressWarnings("unchecked")
          DetailPaneDecorator<Object> decorator = (DetailPaneDecorator<Object>)factory.create(decoratablePane);

          decoratablePane.getStylesheets().clear();
          decoratablePane.getStyleClass().clear();
          decoratablePane.getChildren().clear();

          DetailPane.this.initialize(decoratablePane);

          decorator.dataProperty().set(current);
          decorator.decorate(interactive);
        }
      }
    });

    getChildren().add(decoratablePane);
  }

  private DetailPaneDecoratorFactory findDetailPaneDecoratorFactory(Class<?> cls) {
    int bestInheritanceDepth = -1;
    DetailPaneDecoratorFactory bestFactory = null;

    for(DetailPaneDecoratorFactory factory : detailPaneDecoratorFactories) {
      int inheritanceDepth = getInheritanceDepth(factory.getType());

      if(factory.getType().isAssignableFrom(cls) && inheritanceDepth > bestInheritanceDepth) {
        bestFactory = factory;
        bestInheritanceDepth = inheritanceDepth;
      }
    }

    return bestFactory;
  }

  private int getInheritanceDepth(Class<?> cls) {
    int depth = 0;

    while((cls = cls.getSuperclass()) != null) {
      depth++;
    }

    return depth;
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
