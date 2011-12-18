package hs.mediasystem.framework;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;

/**
 * Represents a specific screen and its content, a page visit.
 */
public class View {
  private final Node screen;
  private final Map<Class<?>, Config<?>> configs = new HashMap<Class<?>, Config<?>>();
  private final String name;

  public View(String name, Node screen, Config<?>... configs) {
    this.name = name;
    this.screen = screen;
    
    for(Config<?> config : configs) {
      this.configs.put(config.getClass(), config);
    }
  }
  
  public String getName() {
    return name;
  }
  
  public Node getScreen() {
    return screen;
  }

  public View copy(String newName) {
    return new View(newName, screen, configs.values().toArray(new Config<?>[] {}));
  }

  public void applyConfig() {
//    for(Config<?> config : configs.values()) {
//      screen.applyConfig(config);
//    }
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
