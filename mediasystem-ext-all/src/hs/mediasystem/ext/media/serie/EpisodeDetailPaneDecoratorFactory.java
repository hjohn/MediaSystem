package hs.mediasystem.ext.media.serie;

import javax.inject.Named;

import hs.mediasystem.screens.selectmedia.DetailPane.DecoratablePane;
import hs.mediasystem.screens.selectmedia.DetailPaneDecorator;
import hs.mediasystem.screens.selectmedia.DetailPaneDecoratorFactory;

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
