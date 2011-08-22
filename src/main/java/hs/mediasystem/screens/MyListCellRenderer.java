package hs.mediasystem.screens;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class MyListCellRenderer implements ListCellRenderer<MenuElement> {
  private final DefaultListCellRenderer defaultTableCellRenderer = new DefaultListCellRenderer();
  
  public MyListCellRenderer() {
  }
  
  @Override
  public Component getListCellRendererComponent(JList<? extends MenuElement> list, MenuElement value, int index, boolean isSelected, boolean cellHasFocus) {
    MenuElement entry = value;
    
    defaultTableCellRenderer.setForeground(list.getForeground());
    defaultTableCellRenderer.getListCellRendererComponent(list, entry.getTitle(), index, isSelected, cellHasFocus);
    
    Dimension preferredSize = defaultTableCellRenderer.getPreferredSize();
    preferredSize.width += 2;  // Workaround for Label text width bug
    defaultTableCellRenderer.setMaximumSize(preferredSize);
    Icon icon = entry.getIcon();
    
    if(!isSelected) {
      defaultTableCellRenderer.setBackground(list.getBackground());
    }
    else {
      defaultTableCellRenderer.setBackground(new Color(255, 0, 0, 120));
    }
    
    if(icon != null) {
      defaultTableCellRenderer.setIcon(icon);
      defaultTableCellRenderer.setIconTextGap(10);
    }
    
    //defaultTableCellRenderer.setOpaque(false);

    return defaultTableCellRenderer;
  }
}