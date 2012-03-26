package hs.mediasystem.framework;

import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.MediaNode;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public interface SelectMediaView {
  ObjectProperty<EventHandler<ActionEvent>> onBack();
  ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect();
  ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected();

  void setRoot(MediaNode root);
  MediaNode getSelectedNode();

  /**
   * Ensures that the given node is focused and visible.  If called with <code>null</code> a default node should be selected.
   *
   * @param mediaNode a node, or <code>null</code> if the default node should be selected
   */
  void setSelectedNode(MediaNode mediaNode);
}
