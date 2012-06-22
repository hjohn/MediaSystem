package hs.mediasystem.screens;

import javafx.scene.layout.Region;

public class SmartMediaNodeCellProvider {
  private final String cellType;

  private Class<?> currentDataType;
  private MediaNodeCell mediaNodeCell;

  public SmartMediaNodeCellProvider(String cellType) {
    this.cellType = cellType;
  }

  public Region getConfiguredGraphic(MediaNode node) {
    Class<?> dataType = node.getDataType();

    if(!dataType.equals(currentDataType)) {
      currentDataType = dataType;
      mediaNodeCell = MediaNodeCellProviderRegistry.get(cellType, dataType).get();
    }

    mediaNodeCell.configureCell(node);

    return (Region)mediaNodeCell;
  }
}
