package hs.mediasystem.fs;

import hs.mediasystem.Constants;
import hs.mediasystem.RomanLiteral;
import hs.mediasystem.framework.Group;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Renderer;
import hs.smartlayout.SmartLayout;
import hs.ui.swing.JPaintablePanel;
import hs.ui.swing.Painter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class DuoLineRenderer implements Renderer<MediaItem> {
  
  private final JPaintablePanel panel = new JPaintablePanel(new SmartLayout(true, 1, 0, 0)) {
    @Override
    public boolean isVisible() {
      return false; // Workaround for bug 6700748 (cursor flickers during D&D)
    }
  };
  
  private final JLabel line1 = new JLabel();
  private final JLabel line2 = new JLabel();

  private String rating;
  private boolean collectionMarker;
  
  public DuoLineRenderer() {
    panel.setPainter(new Painter() {
      @Override
      public void paint(Graphics2D g, int width, int height) {
        Rectangle2D stringBounds = line1.getFont().getStringBounds(line1.getText(), g.getFontRenderContext());

        int lineHeight = (int)stringBounds.getHeight();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setFont(line1.getFont());
        g.setColor(line1.getForeground());
        g.drawString(line1.getText(), line1.getIconTextGap() + 5, (int)-stringBounds.getY() + (line2.getText().isEmpty() ? (Constants.SUBTITLE_HEIGHT - 4)/ 2 : 0));

        if(!line2.getText().isEmpty()) {
          stringBounds = line2.getFont().getStringBounds(line2.getText(), g.getFontRenderContext());

          g.setFont(line2.getFont());
          g.setColor(line2.getForeground());
          g.drawString(line2.getText(), line2.getIconTextGap() + 5, (int)-stringBounds.getY() + lineHeight - 4);
        }

        if(rating != null) {
          Rectangle2D ratingBounds = Constants.LIST_LARGE_FONT.getStringBounds(rating, g.getFontRenderContext());
          
          g.drawString(rating, (int)(width - 40 - ratingBounds.getWidth()), (int)((height - ratingBounds.getHeight()) / 2 - ratingBounds.getY()));
        }
        
        if(collectionMarker) {
          int u = height / 6;
          int x = width - 40;
          int y = height / 2 - u;
          int w = u;
          int h = u * 2;
          
          g.fillPolygon(new int[] {x, x, x + w}, new int[] {y, y + h, y + h / 2}, 3);
        }
      }
    });

    panel.setBorder(new EmptyBorder(5, 10, 5, 10));
  }
  
  enum Style {MOVIE, SEASON, EPISODE, COLLECTION, COLLECTION_ITEM}

  @Override
  public JComponent render(MediaItem mediaItem, boolean hasFocus) {
    NamedItem item = (NamedItem)mediaItem;
    
    Style style;
    
    if(item instanceof Season) {
      style = Style.SEASON;
    }
    else if(item instanceof Group) {
      style = Style.COLLECTION;
    }
    else if(item instanceof Episode) {
      if(item.getSeason() == 0) {
        if(item.getParent() != null) {
          style = Style.COLLECTION_ITEM;
        }
        else {
          style = Style.MOVIE;
        }
      }
      else {
        style = Style.EPISODE;
      }
    }
    else {
      style = Style.MOVIE;
    }
    
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

    Font line1Font = Constants.LIST_LARGE_FONT;
    Font line2Font = Constants.LIST_SMALL_FONT;
    int line1TextGap = 0;
    int line2TextGap = 10;
    collectionMarker = false;
    rating = item.getRating() == null ? null : String.format("%3.1f", item.getRating());
        
    if(style == Style.MOVIE) {
      line1.setText(item.getTitle());
      line2.setText(item.getSubtitle());
    }
    else if(style == Style.COLLECTION_ITEM) {
      line1.setText(item.getTitle() + " " + item.getEpisode());
      if(item.getSubtitle().isEmpty()) {
        line2.setText(item.getTitle() + (item.getEpisode() < 2 ? "" : " " + RomanLiteral.toRomanLiteral(item.getEpisode())));
      }
      else {
        line2.setText(item.getSubtitle());
      }
      line1TextGap = 25;
      line2TextGap = 35;
      line1Font = Constants.LIST_SMALL_FONT;
      line2Font = Constants.LIST_LARGE_FONT;
    }
    else if(style == Style.EPISODE) {
      line1TextGap = 25;
      line2TextGap = 35;
      line1.setText(item.getSeason() + "x" + item.getEpisode() + " : " + item.getSubtitle());
      line2.setText("");
    }
    else if(style == Style.SEASON) {
      line1.setText("Season " + ((Season)item).getTitle());
      line2.setText("");
      collectionMarker = true;
    }
    else if(style == Style.COLLECTION) {
      line1.setText(item.getTitle());
      line2.setText(item.getSubtitle());
      collectionMarker = true;
    }
    
    line1.setFont(line1Font);
    line2.setFont(line2Font);
    line1.setIconTextGap(line1TextGap);
    line2.setIconTextGap(line2TextGap);
    
    panel.setPreferredSize(new Dimension(1, Constants.HEIGHT));
    
    return panel;
  }
  
  @Override
  public MediaItem getPrototypeCellValue() {
    return new NamedItem(null, " ") {
      @Override
      public boolean isRoot() {
        return false;
      }

      @Override
      public MediaTree getRoot() {
        throw new UnsupportedOperationException("Method not implemented");
      }
    };
  }
}