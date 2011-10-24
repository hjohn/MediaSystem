package hs.mediasystem.fs;

import hs.mediasystem.Constants;
import hs.mediasystem.GlowBorder;
import hs.mediasystem.db.LocalItem;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.Renderer;
import hs.ui.controls.Label;
import hs.ui.controls.Picture;
import hs.ui.image.ImageHandle;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public final class MediaItemRenderer implements Renderer<MediaItem> {
  private final Picture pic = new Picture();
  private final Label label = new Label();

  @Override
  public JComponent render(MediaItem item, boolean hasFocus) {
    ImageHandle banner = item.getBanner();
    
    if(banner != null) {
      pic.imageHandle.set(banner);
      pic.bgColor().set(new Color(0, 0, 0, 0));
      pic.minHeight().set(78);
      pic.maxHeight().set(78);

      if(hasFocus) {
        pic.border().set(new GlowBorder(Constants.MAIN_TEXT_COLOR.get(), 4));
      }
      else {
        pic.border().set(new EmptyBorder(4, 4, 4, 4));
      }
      
      return pic.getComponent();
    }
    else {
      label.text().set(item.getTitle());
      label.font().set(Constants.LIST_LARGE_FONT);
      label.fgColor().link(Constants.MAIN_TEXT_COLOR);
      
      if(hasFocus) {
        label.border().set(new GlowBorder(Constants.MAIN_TEXT_COLOR.get(), 4));
      }
      else {
        label.border().set(new EmptyBorder(4, 4, 4, 4));
      }
      
      JLabel label2 = label.getComponent();
      
      label2.setPreferredSize(new Dimension(1, 78));
      
      return label2;
    }
  }

  @Override
  public MediaItem getPrototypeCellValue() {
    return new Serie(null, new LocalItem(null) {{  // TODO not generic enough
      setTitle(" ");
      setLocalTitle(" ");
    }});
  }
}