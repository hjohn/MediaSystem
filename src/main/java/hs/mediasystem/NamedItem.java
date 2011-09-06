package hs.mediasystem;

public class NamedItem implements MediaItem {
  protected NamedItem parent;
  
  private final String title;
  
  public NamedItem(String title) {
    this.title = title;
  }
  
  public final String getTitle() {
    return title;
  }
  
  public NamedItem getParent() {
    return parent;
  }

  @Override
  public boolean isGroup() {
    return false;
  }
}
