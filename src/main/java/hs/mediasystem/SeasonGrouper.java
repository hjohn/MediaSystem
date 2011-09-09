package hs.mediasystem;

public class SeasonGrouper implements Grouper<NamedItem> {

  @Override
  public Object getGroup(NamedItem item) {
    if(item instanceof Episode) {
      return ((Episode)item).getSeason();
    }
    else {
      return "Unknown";
    }
  }

}
