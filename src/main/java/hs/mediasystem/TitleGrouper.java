package hs.mediasystem;

public class TitleGrouper implements Grouper<NamedItem> {

  @Override
  public Object getGroup(NamedItem item) {
    return item.getTitle();
  }

}
