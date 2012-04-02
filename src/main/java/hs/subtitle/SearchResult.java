package hs.subtitle;

public class SearchResult {
  protected final String name;

  public SearchResult(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return getName();
  }
}
