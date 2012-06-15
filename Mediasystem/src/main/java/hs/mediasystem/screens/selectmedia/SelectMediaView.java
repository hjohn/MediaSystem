package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public interface SelectMediaView {
  ObjectProperty<EventHandler<ActionEvent>> onBack();
  ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect();
  ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected();

  void setRoot(MediaNode root);
  ReadOnlyObjectProperty<MediaNode> focusedNodeProperty();

  MediaNode getSelectedNode();

  /**
   * Ensures that the given node is focused and visible.  If called with <code>null</code> a default node should be selected.
   *
   * @param mediaNode a node, or <code>null</code> if the default node should be selected
   */
  void setSelectedNode(MediaNode mediaNode);

  ObservableList<StandardLayoutExtension> availableLayoutExtensionsList();
  ObjectProperty<StandardLayoutExtension> layoutExtensionProperty();
}
