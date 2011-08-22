package hs.mediasystem.screens;

import javax.swing.Icon;

public class MenuElement {
  private final String title;
  private final Icon icon;
  private final String screenName;

  public MenuElement(String title, Icon icon, String screenName) {
    this.title = title;
    this.icon = icon;
    this.screenName = screenName;
  }
  
  public String getTitle() {
    return title;
  }
  
  public Icon getIcon() {
    return icon;
  }

  public String getScreenName() {
    return screenName;
  }
}