package hs.mediasystem.ext.selectmedia.banner;

import hs.mediasystem.framework.MediaRootType;
import hs.mediasystem.screens.selectmedia.StandardLayout;
import hs.mediasystem.screens.selectmedia.StandardLayoutExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

@Named
public class BannerStandardLayoutExtension implements StandardLayoutExtension {
  private static final Set<MediaRootType> SUPPORTED_MEDIA_ROOT_TYPES = Collections.unmodifiableSet(new HashSet<MediaRootType>() {{
    add(MediaRootType.SERIES);
  }});

  private final Provider<BannerStandardLayout> bannerStandardLayoutProvider;

  @Inject
  public BannerStandardLayoutExtension(Provider<BannerStandardLayout> bannerStandardLayoutProvider) {
    this.bannerStandardLayoutProvider = bannerStandardLayoutProvider;
  }

  @Override
  public Set<MediaRootType> getSupportedMediaRootTypes() {
    return SUPPORTED_MEDIA_ROOT_TYPES;
  }

  @Override
  public StandardLayout createLayout() {
    return bannerStandardLayoutProvider.get();
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
