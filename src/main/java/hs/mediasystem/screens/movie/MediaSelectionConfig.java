package hs.mediasystem.screens.movie;

import hs.mediasystem.framework.Config;
import hs.mediasystem.framework.MediaTree;

public class MediaSelectionConfig implements Config<MediaSelectionConfig> {
  private MediaTree mediaTree;
  
  public MediaSelectionConfig(MediaTree mediaTree) {
    this.mediaTree = mediaTree;
  }
  
  public MediaSelectionConfig() {
    this(null);
  }
  
  public MediaTree getMediaTree() {
    return mediaTree;
  }
  
  public void setMediaTree(MediaTree mediaTree) {
    this.mediaTree = mediaTree;
  }

  @Override
  public MediaSelectionConfig copy() {
    MediaSelectionConfig copy = new MediaSelectionConfig();
    
    copy.mediaTree = mediaTree;
    
    return copy;
  }

  @Override
  public Class<?> type() {
    return MediaSelection.class;
  }
}
