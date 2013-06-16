package hs.mediasystem.screens.collection;

public interface DetailPaneDecoratorFactory {
  DetailPaneDecorator<?> create(AbstractDetailPane.DecoratablePane decoratablePane);
  Class<?> getType();
}
