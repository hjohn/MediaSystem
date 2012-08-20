package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.util.AreaPane;
import javafx.beans.property.ObjectProperty;

public interface DetailPaneDecorator {
  void decorate(AreaPane areaPane);
  ObjectProperty<MediaNode> mediaNodeProperty();
}
