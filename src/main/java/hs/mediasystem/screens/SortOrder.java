package hs.mediasystem.screens;

import java.util.Comparator;

public class SortOrder {
  private final String title;
  private final Comparator<? super MediaNode> comparator;

  public SortOrder(String title, Comparator<? super MediaNode> comparator) {
    this.title = title;
    this.comparator = comparator;
  }

  public String getTitle() {
    return title;
  }

  public Comparator<? super MediaNode> getComparator() {
    return comparator;
  }
}
