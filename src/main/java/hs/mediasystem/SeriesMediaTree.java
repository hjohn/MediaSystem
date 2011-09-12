package hs.mediasystem;

import hs.ui.controls.Picture;

import java.awt.Color;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

public class SeriesMediaTree implements MediaTree {
  private final Path root;

  private List<? extends MediaItem> children;
  
  public SeriesMediaTree(Path root) {
    this.root = root;
  }
  
  @Override
  public void refresh() {
    children = null;
  }

  @Override
  public Style getStyle() {
    return Style.BANNER;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      children = new SerieScanner().scan(root);
      //children = new ArrayList<MediaItem>();
    }
    
    return children;
  }

  @Override
  public MediaTree parent() {
    return null;
  }

  @Override
  public Renderer<MediaItem> getRenderer() {
    return new Renderer<MediaItem>() {
      private final Picture pic = new Picture();
      
      @Override
      public JComponent render(MediaItem item, boolean hasFocus) {
        Serie serie = (Serie)item;
        
        pic.image.set(serie.getBanner());
        pic.bgColor().set(new Color(0, 0, 0, 0));
        pic.maxHeight().set(78);
        
        if(hasFocus) {
          pic.border().set(new GlowBorder(Constants.MAIN_TEXT_COLOR.get(), 4));
        }
        else {
          pic.border().set(new EmptyBorder(4, 4, 4, 4));
        }
        
        return pic.getComponent();
      }

      @Override
      public int getPreferredHeight() {
        return 70 + 8; // 758x140
      }
    };
  }
}
