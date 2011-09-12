package hs.mediasystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Extensions implements Iterable<Screen> {
  private final Map<String, Screen> extensions = new HashMap<String, Screen>();
  
  public void addExtension(String name, Screen screen) {
    extensions.put(name, screen);
  }

  public Screen get(String extensionName) {
    return extensions.get(extensionName);
  }

  @Override
  public Iterator<Screen> iterator() {
    return extensions.values().iterator();
  }
}
