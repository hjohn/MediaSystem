package hs.mediasystem.enrich;

import java.util.List;

public interface InstanceEnricher {
  void enrich(Object o);
  void enrich(Object o, List<?> list, String listName);
}
