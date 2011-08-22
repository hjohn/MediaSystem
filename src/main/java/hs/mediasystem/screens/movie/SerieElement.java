package hs.mediasystem.screens.movie;

import hs.mediasystem.db.ItemProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SerieElement extends MovieElement {
  private final List<MovieElement> subElements = new ArrayList<MovieElement>();
  
  public SerieElement(Path path, ItemProvider itemProvider) {
    super(path, itemProvider);
  }

  public SerieElement(String name, ItemProvider itemProvider) {
    super(name, itemProvider);
  }

  public void addSubElement(MovieElement movieElement) {
    subElements.add(movieElement);
  }

  public List<MovieElement> getSubElements() {
    return subElements;
  }
}

