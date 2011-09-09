package hs.mediasystem;

import hs.smartlayout.SmartLayout;
import hs.ui.swing.JPaintablePanel;
import hs.ui.swing.Painter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class DuoLineRenderer implements Renderer<MediaItem> {
  private static final int HEIGHT = 48;
  private static final int SUBTITLE_HEIGHT = 16;
  private static final int TITLE_HEIGHT = HEIGHT - SUBTITLE_HEIGHT;
  
  private final JPaintablePanel panel = new JPaintablePanel(new SmartLayout(true, 1, 0, 0)) {
    @Override
    public boolean isVisible() {
      return false; // Workaround for bug 6700748 (cursor flickers during D&D)
    }
  };
  
  private final JLabel line1 = new JLabel();
  private final JLabel line2 = new JLabel();
  
  public DuoLineRenderer() {
    panel.setPainter(new Painter() {
      @Override
      public void paint(Graphics2D g, int width, int height) {
        Rectangle2D stringBounds = line1.getFont().getStringBounds(line1.getText(), g.getFontRenderContext());

        int lineHeight = (int)stringBounds.getHeight();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(line1.getFont());
        g.setColor(line1.getForeground());
        g.drawString(line1.getText(), line1.getIconTextGap() + 5, (int)-stringBounds.getY() + (line2.getText().isEmpty() ? (SUBTITLE_HEIGHT - 4)/ 2 : 0));

        if(!line2.getText().isEmpty()) {
          stringBounds = line2.getFont().getStringBounds(line2.getText(), g.getFontRenderContext());

          g.setFont(line2.getFont());
          g.setColor(line2.getForeground());
          g.drawString(line2.getText(), line2.getIconTextGap() + 5, (int)-stringBounds.getY() + lineHeight - 4);
        }
      }
    });

    panel.setBorder(new EmptyBorder(5, 10, 5, 10));
  }

  @Override
  public JComponent render(MediaItem item, boolean hasFocus) {
    if(!hasFocus) {
      line1.setForeground(Constants.MAIN_TEXT_COLOR.get());
      line2.setForeground(Constants.MAIN_TEXT_COLOR.get());
      panel.setBackground(new Color(0, 0, 0, 0));
    }
    else {
      line1.setForeground(Constants.MAIN_TEXT_COLOR.get().brighter());
      line2.setForeground(Constants.MAIN_TEXT_COLOR.get().brighter());
      panel.setBackground(new Color(255, 255, 255, 60));
    }

    line1.setFont(new Font("Sans Serif", Font.PLAIN, TITLE_HEIGHT - 4));
    line2.setFont(new Font("Sans Serif", Font.PLAIN, SUBTITLE_HEIGHT - 4));
    line1.setIconTextGap(0);
    line2.setIconTextGap(10);

    if(item instanceof Episode) {
      Episode episode = (Episode)item;
      boolean subItemDisplay = episode.getParent() != null;

      if(subItemDisplay) {
        line1.setIconTextGap(25);
        line2.setIconTextGap(35);
      }
      
      if(episode.getSeason() == 0) {
        if(subItemDisplay) {
          line1.setText(episode.getTitle() + " " + episode.getEpisode());
          if(episode.getSubtitle().isEmpty()) {
            line2.setText(episode.getTitle() + (episode.getEpisode() < 2 ? "" : " " + RomanLiteral.toRomanLiteral(episode.getEpisode())));
          }
          else {
            line2.setText(episode.getSubtitle());
          }
          line2.setFont(new Font("Sans Serif", Font.PLAIN, TITLE_HEIGHT - 4));
          line1.setFont(new Font("Sans Serif", Font.PLAIN, SUBTITLE_HEIGHT - 4));
        }
        else {
          line1.setText(episode.getTitle());
          line2.setText(episode.getSubtitle());
        }
      }
      else {
        line1.setText(episode.getSeason() + "x" + episode.getEpisode() + " : " + episode.getSubtitle());
        line2.setText("");
      }
    }
    else if(item instanceof Season) {
      line1.setText("Season " + ((Season)item).getTitle());
      line2.setText("");
    }
    else if(item instanceof NamedItem) {
      line1.setText(((NamedItem)item).getTitle());
      line2.setText("");
    }
    
    return panel;
  }
}