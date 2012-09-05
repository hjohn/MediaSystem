package hs.mediasystem.screens.selectmedia;

public interface DetailPaneDecoratorFactory {
  DetailPaneDecorator<?> create(DetailPane.DecoratablePane decoratablePane);
}
