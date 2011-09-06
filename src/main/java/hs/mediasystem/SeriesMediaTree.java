package hs.mediasystem;

import hs.ui.controls.Picture;

import java.awt.Color;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JComponent;

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
  public MediaTree getRoot(MediaItem item) {
    throw new UnsupportedOperationException("Method not implemented");
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
        pic.bgColor().set(Color.BLACK);
        
        return pic.getComponent();
      }
    };
  }
}
