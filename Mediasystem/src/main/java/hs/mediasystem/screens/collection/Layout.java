package hs.mediasystem.screens.collection;

import javafx.scene.Node;

public interface Layout<C, P> {
  String getId();
  String getTitle();
  boolean isSuitableFor(C content);

  Node create(P presentation);
}
