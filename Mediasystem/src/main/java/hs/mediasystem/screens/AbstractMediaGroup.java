package hs.mediasystem.screens;

public abstract class AbstractMediaGroup implements MediaGroup {
  private final String id;
  private final String title;
  private final boolean showTopLevelExpanded;

  public AbstractMediaGroup(String id, String title, boolean showTopLevelExpanded) {
    assert id != null && !id.contains("/") && !id.contains(":");

    this.id = id;
    this.title = title;
    this.showTopLevelExpanded = showTopLevelExpanded;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean showTopLevelExpanded() {
    return showTopLevelExpanded;
  }
}