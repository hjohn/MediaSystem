package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.player.Subtitle;
import hs.mediasystem.util.StringConverter;

public class SubtitleStringConverter implements StringConverter<Subtitle> {

  @Override
  public String toString(Subtitle object) {
    return object.getDescription();
  }

}
