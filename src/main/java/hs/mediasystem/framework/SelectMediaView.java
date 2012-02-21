package hs.mediasystem.framework;

import hs.mediasystem.screens.MediaItemEvent;
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
  ObjectProperty<EventHandler<ActionEvent>> onBack();
  ObjectProperty<EventHandler<MediaItemEvent>> onItemAlternateSelect();
  ObjectProperty<EventHandler<MediaItemEvent>> onItemSelected();

  void setCellFactory(Callback<TreeView<MediaItem>, TreeCell<MediaItem>> cellFactory);  // TODO remove

  TreeItem<MediaItem> getRoot();           // TODO no TreeItem wrapper
  void setRoot(TreeItem<MediaItem> root);  // TODO no TreeItem wrapper

  ObjectProperty<Node> activeFilterItemProperty();
  ObservableList<Node> filterItemsProperty();
}
