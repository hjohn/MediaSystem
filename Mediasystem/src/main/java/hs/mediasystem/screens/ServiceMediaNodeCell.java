package hs.mediasystem.screens;

import hs.mediasystem.util.PropertyClassEq;
import hs.mediasystem.util.ServiceTracker;
import javafx.scene.layout.Region;

public class ServiceMediaNodeCell {
  private final ServiceTracker<MediaNodeCellProvider> cellProviderTracker;

  private Class<?> currentDataType;
  private MediaNodeCell mediaNodeCell;

  public ServiceMediaNodeCell(ServiceTracker<MediaNodeCellProvider> cellProviderTracker) {
    this.cellProviderTracker = cellProviderTracker;
  }

  public void configureGraphic(MediaNode node) {
    if(mediaNodeCell != null) {
      mediaNodeCell.detach();
    }

    if(node != null) {
      Class<?> dataType = node.getDataType();

      if(!dataType.equals(currentDataType)) {
        currentDataType = dataType;
        mediaNodeCell = cellProviderTracker.getService(new PropertyClassEq("mediasystem.class", dataType)).get();
      }

      mediaNodeCell.attach(node);
    }
    else {
      mediaNodeCell = null;
    }
  }

  public Region getGraphic() {
    return (Region)mediaNodeCell;
  }
}
