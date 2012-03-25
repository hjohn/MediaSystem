package hs.mediasystem;

import java.util.LinkedHashMap;
import java.util.Map;

public class StateCache {
  private final Map<String, String> states = new LinkedHashMap<String, String>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
      return size() > 100;
    }
  };

  public void putState(String key, String state) {
    System.out.println("[FINE] StateCache.putState() - adding state: " + key + " => " + state);
    states.put(key, state);
  }

  public String getState(String key) {
    return states.get(key);
  }
}
