package hs.mediasystem.screens.collection;

import hs.mediasystem.util.AreaPane;

import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;

public abstract class AbstractDetailPane extends StackPane {
  public final ObjectProperty<Object> content = new SimpleObjectProperty<>();
  public final BooleanProperty interactive = new SimpleBooleanProperty(false);

  private final DecoratablePane decoratablePane = new DecoratablePane(content);
  private final Set<DetailPaneDecoratorFactory> detailPaneDecoratorFactories;

  private DetailPaneDecorator<Object> currentDetailPaneDecorator;

  public AbstractDetailPane(Set<DetailPaneDecoratorFactory> detailPaneDecoratorFactories) {
    this.detailPaneDecoratorFactories = detailPaneDecoratorFactories;

    getStylesheets().add("collection/detail-pane.css");
    getStyleClass().add("detail-pane");

    mouseTransparentProperty().bind(interactive.not());

    content.addListener(new ChangeListener<Object>() {
      @Override
      public void changed(ObservableValue<? extends Object> observable, Object old, Object current) {

        /*
         * Reset decoratable pane to show default content:
         */

        decoratablePane.decoratorContent.set(null);
      }
    });

    decoratablePane.finalContent.addListener(new ChangeListener<Object>() {
      @Override
      public void changed(ObservableValue<? extends Object> observable, Object old, Object current) {
        if(currentDetailPaneDecorator != null) {
          currentDetailPaneDecorator.dataProperty().set(null);
          currentDetailPaneDecorator = null;
        }

        if(current != null) {
          DetailPaneDecoratorFactory factory = findDetailPaneDecoratorFactory(current.getClass());

          currentDetailPaneDecorator = (DetailPaneDecorator<Object>)factory.create(decoratablePane);

          decoratablePane.getStylesheets().clear();
          decoratablePane.getStyleClass().clear();
          decoratablePane.getChildren().clear();

          AbstractDetailPane.this.initialize(decoratablePane);

          currentDetailPaneDecorator.dataProperty().set(current);
          currentDetailPaneDecorator.decorate(interactive.get());  // TODO this is static, while mouseTransparent is dynamic
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
    Class<?> parent = cls;
    int depth = 0;

    while((parent = parent.getSuperclass()) != null) {
      depth++;
    }

    return depth;
  }


  /**
   * Set up the AreaPane with default groups.
   */
  protected abstract void initialize(DecoratablePane decoratablePane);

  /**
   * Extension of AreaPane that allows the final content to be overriden by user
   * interaction.  Normally it will display the linked content, but if decoratorContent
   * is set (for example because of a selection made), this will be shown instead.
   *
   * TODO this does not seem to be related specifically to this subclass... why is it here?
   */
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
