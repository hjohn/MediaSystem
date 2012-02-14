package hs.mediasystem.framework;

import hs.mediasystem.screens.TreeItemEvent;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public interface SelectMediaView {
  ObjectProperty<MediaItem> mediaItemProperty();
  ObjectProperty<EventHandler<ActionEvent>> onBack();
  ObjectProperty<EventHandler<TreeItemEvent<MediaItem>>> onItemAlternateSelect();
  ObjectProperty<EventHandler<TreeItemEvent<MediaItem>>> onItemSelected();
  ObjectProperty<EventHandler<TreeItemEvent<MediaItem>>> onItemFocused();

  void setCellFactory(Callback<TreeView<MediaItem>, TreeCell<MediaItem>> cellFactory);

  TreeItem<MediaItem> getRoot();
  void setRoot(TreeItem<MediaItem> root);

  ObjectProperty<Node> activeFilterItemProperty();
  ObservableList<Node> filterItemsProperty();
}
