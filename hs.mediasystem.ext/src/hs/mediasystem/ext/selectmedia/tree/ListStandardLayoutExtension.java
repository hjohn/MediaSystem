package hs.mediasystem.ext.selectmedia.tree;

import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.screens.selectmedia.StandardLayout;
import hs.mediasystem.screens.selectmedia.StandardLayoutExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;

public class ListStandardLayoutExtension implements StandardLayoutExtension {
  private static final Set<MediaRootType> SUPPORTED_MEDIA_ROOT_TYPES = Collections.unmodifiableSet(new HashSet<MediaRootType>() {{
    add(MediaRootType.MOVIES);
    add(MediaRootType.SERIE_EPISODES);
    add(MediaRootType.SERIES);
  }});

  private volatile BundleContext bundleContext;

  @Override
  public Set<MediaRootType> getSupportedMediaRootTypes() {
    return SUPPORTED_MEDIA_ROOT_TYPES;
  }

  @Override
  public StandardLayout createLayout() {
    return new ListStandardLayout(bundleContext);
  }

  @Override
  public String getTitle() {
    return "List View";
  }

  @Override
  public String getId() {
    return "listView";
  }
}
