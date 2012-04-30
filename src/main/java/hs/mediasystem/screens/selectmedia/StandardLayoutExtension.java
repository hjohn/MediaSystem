package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.fs.MediaRootType;

import java.util.Set;

public interface StandardLayoutExtension {
  Set<MediaRootType> getSupportedMediaRootTypes();
  StandardLayout createLayout();
  String getTitle();
}
