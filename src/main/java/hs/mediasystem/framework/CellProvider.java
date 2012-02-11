package hs.mediasystem.framework;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public interface CellProvider<T> {
  Node configureCell(TreeItem<T> item);
}
