package hs.mediasystem.fs;

import javafx.scene.Node;

public interface CellProvider<T> {
  Node configureCell(T item);
}
