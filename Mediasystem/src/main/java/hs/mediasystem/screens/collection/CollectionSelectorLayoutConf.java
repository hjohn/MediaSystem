package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.MediaRoot;
import javafx.scene.Node;

public interface CollectionSelectorLayoutConf {
  String getId();
  String getTitle();
  boolean isSuitableFor(MediaRoot mediaRoot);

  Node create(CollectionSelectorPresentation presentation);
}
