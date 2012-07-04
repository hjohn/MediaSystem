package hs.mediasystem.screens;

import hs.mediasystem.util.PropertyClassEq;
import hs.mediasystem.util.ServiceTracker;
import javafx.scene.layout.Region;

public class ServiceMediaNodeCellProvider {
  private final ServiceTracker<MediaNodeCellProvider> cellProviderTracker;

  private Class<?> currentDataType;
  private MediaNodeCell mediaNodeCell;

  public ServiceMediaNodeCellProvider(ServiceTracker<MediaNodeCellProvider> cellProviderTracker) {
    this.cellProviderTracker = cellProviderTracker;
  }

  public Region getConfiguredGraphic(MediaNode node) {
    Class<?> dataType = node.getDataType();

    if(!dataType.equals(currentDataType)) {
      currentDataType = dataType;
      mediaNodeCell = cellProviderTracker.getService(new PropertyClassEq("mediasystem.class", dataType)).get();
    }

    mediaNodeCell.configureCell(node);

    return (Region)mediaNodeCell;
  }
}
