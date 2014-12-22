package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.util.StringConverter;

public class AudioTrackStringConverter implements StringConverter<AudioTrack> {

  @Override
  public String toString(AudioTrack object) {
    return object.getDescription();
  }

}
