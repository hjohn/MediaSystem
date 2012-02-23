package hs.mediasystem.framework;

import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.MediaNode;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public interface SelectMediaView {
  ObjectProperty<EventHandler<ActionEvent>> onBack();
  ObjectProperty<EventHandler<MediaNodeEvent>> onItemAlternateSelect();
  ObjectProperty<EventHandler<MediaNodeEvent>> onItemSelected();

  void setRoot(MediaNode root);
}
