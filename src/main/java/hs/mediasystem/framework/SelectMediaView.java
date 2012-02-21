package hs.mediasystem.framework;

import hs.mediasystem.screens.MediaItemEvent;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public interface SelectMediaView {
  ObjectProperty<EventHandler<ActionEvent>> onBack();
  ObjectProperty<EventHandler<MediaItemEvent>> onItemAlternateSelect();
  ObjectProperty<EventHandler<MediaItemEvent>> onItemSelected();

  void setRoot(MediaItem root);
}
