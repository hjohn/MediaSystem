package hs.mediasystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Season extends NamedItem implements Group {
  private final List<NamedItem> children = new ArrayList<NamedItem>();
  
  public Season(String title) {
    super(title);
  }

  public void add(NamedItem child) {
    children.add(child);
    child.parent = this;
  }

  public int size() {
    return children.size();
  }

  @Override
  public Collection<? extends NamedItem> children() {
    return Collections.unmodifiableCollection(children);
  }
}
