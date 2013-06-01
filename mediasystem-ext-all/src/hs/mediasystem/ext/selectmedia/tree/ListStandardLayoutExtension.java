package hs.mediasystem.ext.selectmedia.tree;

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
public class ListStandardLayoutExtension implements StandardLayoutExtension {
  private static final Set<MediaRootType> SUPPORTED_MEDIA_ROOT_TYPES = Collections.unmodifiableSet(new HashSet<MediaRootType>() {{
    add(MediaRootType.MOVIES);
    add(MediaRootType.SERIE_EPISODES);
    add(MediaRootType.SERIES);
  }});

  private final Provider<ListStandardLayout> listStandardLayoutProvider;

  @Inject
  public ListStandardLayoutExtension(Provider<ListStandardLayout> listStandardLayoutProvider) {
    this.listStandardLayoutProvider = listStandardLayoutProvider;
  }

  @Override
  public Set<MediaRootType> getSupportedMediaRootTypes() {
    return SUPPORTED_MEDIA_ROOT_TYPES;
  }

  @Override
  public StandardLayout createLayout() {
    return listStandardLayoutProvider.get();
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
