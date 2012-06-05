package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.BasicNode;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;

public interface ListPane extends BasicNode {
  ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected();
  ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect();
  ObjectBinding<MediaNode> mediaNodeBinding();

  void setRoot(MediaNode root);
  MediaNode getSelectedNode();
  void setSelectedNode(MediaNode mediaNode);
}