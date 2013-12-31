package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.framework.Media;

public class MediaLayout extends AbstractDetailViewLayout<Media> {

  @Override
  public Class<?> getContentClass() {
    return Media.class;
  }

  @Override
  protected DetailPane<Media> createDetailPane(DetailPanePresentation presentation) {
    return MediaDetailPane.create(presentation.getAreaLayout(), presentation.isInteractive());
  }
}
