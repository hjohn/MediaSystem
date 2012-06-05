package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.fs.MediaRootType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BannerStandardLayoutExtension implements StandardLayoutExtension {
  private static final Set<MediaRootType> SUPPORTED_MEDIA_ROOT_TYPES = Collections.unmodifiableSet(new HashSet<MediaRootType>() {{
    add(MediaRootType.SERIES);
  }});

  @Override
  public Set<MediaRootType> getSupportedMediaRootTypes() {
    return SUPPORTED_MEDIA_ROOT_TYPES;
  }

  @Override
  public StandardLayout createLayout() {
    return new BannerStandardLayout();
  }

  @Override
  public String getTitle() {
    return "Banner View";
  }
}
