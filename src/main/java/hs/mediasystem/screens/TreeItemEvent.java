package hs.mediasystem.screens;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.TreeItem;

public class TreeItemEvent<T> extends Event {
  private final TreeItem<T> treeItem;

  public TreeItemEvent(TreeItem<T> treeItem) {
    super(EventType.ROOT);
    this.treeItem = treeItem;
  }

  public TreeItem<T> getTreeItem() {
    return treeItem;
  }
}