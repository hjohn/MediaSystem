package hs.mediasystem.framework;

import hs.mediasystem.framework.player.Player;
import javafx.beans.property.ObjectProperty;

public interface PlaybackOverlayView {
  ObjectProperty<MediaItem> mediaItemProperty();
  ObjectProperty<Player> playerProperty();
}
