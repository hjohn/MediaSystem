package hs.mediasystem.fs;

import hs.mediasystem.framework.Grouper;

public class TitleGrouper implements Grouper<NamedItem> {

  @Override
  public Object getGroup(NamedItem item) {
    return item.getTitle();
  }

}
