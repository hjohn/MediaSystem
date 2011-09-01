package hs.mediasystem;

public class TitleGrouper implements Grouper {

  @Override
  public Object getGroup(Episode episode) {
    return episode.getTitle();
  }

}
