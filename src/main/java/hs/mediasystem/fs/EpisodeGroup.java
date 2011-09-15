package hs.mediasystem.fs;

import hs.mediasystem.framework.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EpisodeGroup extends NamedItem implements Group {
  private final List<NamedItem> children = new ArrayList<NamedItem>();
  
  public EpisodeGroup(String title) {
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
