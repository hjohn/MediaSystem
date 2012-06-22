package hs.mediasystem.screens;

import hs.mediasystem.framework.Media;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

public class MediaNodeCellProviderRegistry {
  public static final String HORIZONTAL_CELL = "HorizontalCell";  // TODO temporary

  private static final Map<String, Map<Class<?>, Provider<? extends MediaNodeCell>>> REGISTRY = new HashMap<>();

  static {
    register(HORIZONTAL_CELL, Media.class, new Provider<StandardCell>() {
      @Override
      public StandardCell get() {
        return new StandardCell();
      }
    });
  }

  public static void register(String cellType, Class<?> dataType, Provider<? extends MediaNodeCell> factory) {
    Map<Class<?>, Provider<? extends MediaNodeCell>> providers = REGISTRY.get(cellType);

    if(providers == null) {
      providers = new HashMap<>();
      REGISTRY.put(cellType, providers);
    }

    providers.put(dataType, factory);
  }

  public static Provider<? extends MediaNodeCell> get(String cellType, Class<?> dataType) {
    Map<Class<?>, Provider<? extends MediaNodeCell>> providers = REGISTRY.get(cellType);

    if(providers != null) {
      Provider<? extends MediaNodeCell> provider = null;
      Class<?> cls = dataType;

      while(cls != null && (provider = providers.get(cls)) == null) {
        cls = cls.getSuperclass();
      }

      return provider;
    }

    return null;
  }
}
