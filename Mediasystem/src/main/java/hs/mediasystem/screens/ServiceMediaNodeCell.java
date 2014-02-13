package hs.mediasystem.screens;

import java.util.Set;

import javafx.scene.layout.Region;

public class ServiceMediaNodeCell {
  private final Set<MediaNodeCellProvider> mediaNodeCellProviders;

  private Class<?> currentDataType;
  private MediaNodeCell mediaNodeCell;

  public ServiceMediaNodeCell(Set<MediaNodeCellProvider> mediaNodeCellProviders) {
    this.mediaNodeCellProviders = mediaNodeCellProviders;
  }

  public void configureGraphic(MediaNode node) {
    if(mediaNodeCell != null) {
      mediaNodeCell.detach();
    }

    if(node != null) {
      Class<?> dataType = node.getMedia().getClass();

      if(!dataType.equals(currentDataType)) {
        currentDataType = dataType;
        mediaNodeCell = findMediaNodeCell(dataType);
      }

      mediaNodeCell.attach(node);
    }
    else {
      mediaNodeCell = null;
      currentDataType = null;
    }
  }

  private MediaNodeCell findMediaNodeCell(Class<?> cls) {
    int bestInheritanceDepth = -1;
    MediaNodeCellProvider bestProvider = null;

    for(MediaNodeCellProvider provider : mediaNodeCellProviders) {
      int inheritanceDepth = getInheritanceDepth(provider.getMediaType());

      if(provider.getMediaType().isAssignableFrom(cls) && inheritanceDepth > bestInheritanceDepth) {
        bestProvider = provider;
        bestInheritanceDepth = inheritanceDepth;
      }
    }

    return bestProvider == null ? null : bestProvider.get();
  }

  private int getInheritanceDepth(Class<?> cls) {
    int depth = 0;

    while((cls = cls.getSuperclass()) != null) {
      depth++;
    }

    return depth;
  }

  public Region getGraphic() {
    return (Region)mediaNodeCell;
  }
}
