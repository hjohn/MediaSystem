package hs.mediasystem.screens;

import hs.mediasystem.framework.View;

import javax.swing.Icon;

public class MenuElement {
  private final String title;
  private final Icon icon;
  private final View view;

  public MenuElement(String title, Icon icon, View view) {
    this.title = title;
    this.icon = icon;
    this.view = view;
  }
  
  public String getTitle() {
    return title;
  }
  
  public Icon getIcon() {
    return icon;
  }

  public View getView() {
    return view;
  }
}