package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.collection.detail.DetailPane;
import hs.mediasystem.screens.collection.detail.DetailPanePresentation;
import hs.mediasystem.screens.collection.detail.MediaLayout;

import javax.inject.Named;

@Named
public class EpisodeLayout extends MediaLayout {

  @Override
  public Class<?> getContentClass() {
    return Episode.class;
  }

  @Override
  protected DetailPane<Media> createDetailPane(DetailPanePresentation presentation) {
    return EpisodeDetailPane.create(presentation.getAreaLayout(), presentation.isInteractive());
  }
}
