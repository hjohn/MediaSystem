package hs.mediasystem.ext.media.serie;

import javax.inject.Named;

import hs.mediasystem.screens.collection.DetailPaneDecorator;
import hs.mediasystem.screens.collection.DetailPaneDecoratorFactory;
import hs.mediasystem.screens.collection.AbstractDetailPane.DecoratablePane;

@Named
public class EpisodeDetailPaneDecoratorFactory implements DetailPaneDecoratorFactory {

  @Override
  public DetailPaneDecorator<?> create(DecoratablePane decoratablePane) {
    return new EpisodeDetailPaneDecorator(decoratablePane);
  }

  @Override
  public Class<?> getType() {
    return Episode.class;
  }
}
