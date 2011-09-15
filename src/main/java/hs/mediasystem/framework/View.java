package hs.mediasystem.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a specific screen and its content, a page visit.
 */
public class View {
  private final Screen screen;
  private final Map<Class<?>, Config<?>> configs = new HashMap<Class<?>, Config<?>>();

  public View(Screen screen, Config<?>... configs) {
    this.screen = screen;
    
    for(Config<?> config : configs) {
      this.configs.put(config.getClass(), config);
    }
  }
  
  public Screen getScreen() {
    return screen;
  }

  public View copy() {
    return new View(screen, configs.values().toArray(new Config<?>[] {}));
  }

  public void applyConfig() {
    for(Config<?> config : configs.values()) {
      screen.applyConfig(config);
    }
  }
  
  public <T extends Config<T>> T replaceConfig(Class<T> cls) {
    @SuppressWarnings("unchecked")
    T config = (T)configs.get(cls);
    
    if(config == null) {
      try {
        config = cls.newInstance();
      }
      catch(InstantiationException e) {
        throw new RuntimeException(e);
      }
      catch(IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    else {
      config = config.copy();
    }
    
    configs.put(config.getClass(), config);
    
    return config;
  }
}
