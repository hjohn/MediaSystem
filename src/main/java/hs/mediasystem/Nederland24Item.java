package hs.mediasystem;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.ui.image.ImageHandle;

public class Nederland24Item implements MediaItem {
  private final MediaTree root;
  private final String title;
  private final String uri;

  public Nederland24Item(MediaTree root, String title, String uri) {
    this.root = root;
    this.title = title;
    this.uri = uri;
  }
  
  @Override
  public boolean isRoot() {
    return false;
  }
  
  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public MediaTree getRoot() {
    return root;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getSubtitle() {
    return "";
  }

  @Override
  public int getSeason() {
    return 0;
  }

  @Override
  public int getEpisode() {
    return 1;
  }

  @Override
  public ImageHandle getBanner() {
    return null;
  }

  @Override
  public String getUri() {
    return uri;
  }

  @Override
  public ImageHandle getPoster() {
    return null;
  }

  @Override
  public ImageHandle getBackground() {
    return null;
  }

  @Override
  public String getReleaseYear() {
    return null;
  }

  @Override
  public String getPlot() {
    return null;
  }

  @Override
  public void loadData() {
  }

  @Override
  public boolean isDataLoaded() {
    return true;
  }
}
