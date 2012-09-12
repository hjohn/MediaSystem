package hs.mediasystem.ext.selectmedia.banner;

import hs.mediasystem.framework.MediaRootType;
import hs.mediasystem.screens.selectmedia.StandardLayout;
import hs.mediasystem.screens.selectmedia.StandardLayoutExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleContext;

public class BannerStandardLayoutExtension implements StandardLayoutExtension {
  private static final Set<MediaRootType> SUPPORTED_MEDIA_ROOT_TYPES = Collections.unmodifiableSet(new HashSet<MediaRootType>() {{
    add(MediaRootType.SERIES);
  }});

  private volatile BundleContext bundleContext;

  @Override
  public Set<MediaRootType> getSupportedMediaRootTypes() {
    return SUPPORTED_MEDIA_ROOT_TYPES;
  }

  @Override
  public StandardLayout createLayout() {
    return new BannerStandardLayout(bundleContext);
  }

  @Override
  public String getTitle() {
    return "Banner View";
  }

  @Override
  public String getId() {
    return "bannerView";
  }
}
