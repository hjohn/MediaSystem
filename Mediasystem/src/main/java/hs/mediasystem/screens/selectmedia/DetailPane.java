package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.util.BasicNode;
import javafx.beans.property.ObjectProperty;

public interface DetailPane extends BasicNode {
  ObjectProperty<MediaNode> mediaNodeProperty();
}
