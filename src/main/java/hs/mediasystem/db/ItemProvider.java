package hs.mediasystem.db;

import hs.mediasystem.screens.movie.Element;

// TODO rename to something "enricher" as it enriches the data
public interface ItemProvider {
  public Item getItem(Element element) throws ItemNotFoundException;
}
