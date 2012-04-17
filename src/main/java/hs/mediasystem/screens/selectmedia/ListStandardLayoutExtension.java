package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.fs.MediaRootType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ListStandardLayoutExtension implements StandardLayoutExtension {
  private static final Set<MediaRootType> SUPPORTED_MEDIA_ROOT_TYPES = Collections.unmodifiableSet(new HashSet<MediaRootType>() {{
    add(MediaRootType.MOVIES);
    add(MediaRootType.SERIE_EPISODES);
  }});

  @Override
  public Set<MediaRootType> getSupportedMediaRootTypes() {
    return SUPPORTED_MEDIA_ROOT_TYPES;
  }

  @Override
  public StandardLayout createLayout() {
    return new ListStandardLayout();
  }
}
