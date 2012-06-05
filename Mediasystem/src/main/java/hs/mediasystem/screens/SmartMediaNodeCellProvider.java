package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaNodeCell;
import hs.mediasystem.framework.MediaNodeCellProviderRegistry;
import javafx.scene.Node;

public class SmartMediaNodeCellProvider {
  private final String cellType;

  private Class<?> currentDataType;
  private MediaNodeCell mediaNodeCell;

  public SmartMediaNodeCellProvider(String cellType) {
    this.cellType = cellType;
  }

  public Node getConfiguredGraphic(MediaNode node) {
    Class<?> dataType = node.getDataType();

    if(!dataType.equals(currentDataType)) {
      currentDataType = dataType;
      mediaNodeCell = MediaNodeCellProviderRegistry.get(cellType, dataType).get();
    }

    mediaNodeCell.configureCell(node);

    return (Node)mediaNodeCell;
  }
}
