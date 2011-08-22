package hs.mediasystem.screens;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class MyCellRenderer implements TableCellRenderer {
  private final DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
  
  public MyCellRenderer() {
  }
  
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    MenuElement entry = (MenuElement)value;
    
    defaultTableCellRenderer.setForeground(table.getForeground());
    defaultTableCellRenderer.getTableCellRendererComponent(table, entry.getTitle(), isSelected, hasFocus, row, column);
    
    Dimension preferredSize = defaultTableCellRenderer.getPreferredSize();
    preferredSize.width += 2;  // Workaround for Label text width bug
    defaultTableCellRenderer.setMaximumSize(preferredSize);
    Icon icon = entry.getIcon();
    
    if(!isSelected) {
      defaultTableCellRenderer.setBackground(table.getBackground());
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