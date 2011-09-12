package hs.mediasystem.screens;

import hs.mediasystem.Screen;

import javax.swing.Icon;

public class MenuElement {
  private final String title;
  private final Icon icon;
  private final Screen screen;

  public MenuElement(String title, Icon icon, Screen screen) {
    this.title = title;
    this.icon = icon;
    this.screen = screen;
  }
  
  public String getTitle() {
    return title;
  }
  
  public Icon getIcon() {
    return icon;
  }

  public Screen getScreen() {
    return screen;
  }
}