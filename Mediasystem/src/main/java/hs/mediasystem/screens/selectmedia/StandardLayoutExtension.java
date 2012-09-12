package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.MediaRootType;

import java.util.Set;

public interface StandardLayoutExtension {
  Set<MediaRootType> getSupportedMediaRootTypes();
  StandardLayout createLayout();
  String getId();
  String getTitle();
}
