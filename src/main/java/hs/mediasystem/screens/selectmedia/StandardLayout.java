package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;

public interface StandardLayout {
  ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect();
  ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected();
  ObjectBinding<MediaItem> mediaItemBinding();

  void setRoot(MediaNode root);
  MediaNode getSelectedNode();

  /**
   * Ensures that the given node is focused and visible.  If called with <code>null</code> a default node should be selected.
   *
   * @param mediaNode a node, or <code>null</code> if the default node should be selected
   */
  void setSelectedNode(MediaNode mediaNode);
}