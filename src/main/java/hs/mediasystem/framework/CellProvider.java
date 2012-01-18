package hs.mediasystem.framework;

import javafx.scene.Node;

public interface CellProvider<T> {
  Node configureCell(T item);
}
