package hs.mediasystem.framework;


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Groups {
  public static <T> Collection<List<T>> group(List<? extends T> list, Grouper<T> grouper) {
    Map<Object, List<T>> groups = new LinkedHashMap<>();

    for(T item : list) {
      Object key = grouper.getGroup(item);

      List<T> group = groups.get(key);

      if(group == null) {
        group = new ArrayList<>();
        groups.put(key, group);
      }

      group.add(item);
    }

    return groups.values();
  }
}
