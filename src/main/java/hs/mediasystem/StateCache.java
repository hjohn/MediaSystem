package hs.mediasystem;

import hs.mediasystem.framework.State;

import java.util.LinkedHashMap;
import java.util.Map;

public class StateCache {
  private final Map<Object, State> states = new LinkedHashMap<Object, State>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Object,State> eldest) {
      return size() > 100;
    }
  };
  
  public void putState(Object key, State state) {
    states.put(key, state);
  }
  
  public State getState(Object key) {
    return states.get(key);
  }
}
