package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.actions.Expose;
import hs.mediasystem.framework.actions.Range;
import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;

import java.nio.file.Path;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

import javax.inject.Inject;

public class PlayerPresentation {
  @Expose
  public final LongProperty position = new SimpleLongProperty() {
    @Override
    public void set(long newValue) {
      super.set(clamp(newValue, 0L, player.getLength()));
    }
  };

  @Expose
  public final BooleanProperty paused = new SimpleBooleanProperty();

  @Expose
  public final BooleanProperty muted = new SimpleBooleanProperty();

  @Expose @Range(min = 0, max = 100, step = 5)
  public final IntegerProperty volume = new SimpleIntegerProperty() {
    @Override
    public void set(int newValue) {
      super.set(clamp(newValue, 0, 100));
    }
  };

  @Expose @Range(min = 0, max = 2, step = 0.01)
  public final FloatProperty brightness = new SimpleFloatProperty() {
    @Override
    public void set(float newValue) {
      super.set(clamp(newValue, 0.0f, 2.0f));
    }
  };

  @Expose @Range(min = 0.1, max = 4, step = 0.1)
  public final FloatProperty rate = new SimpleFloatProperty() {
    @Override
    public void set(float newValue) {
      super.set(clamp(newValue, 0.1f, 4.0f));
    }
  };

  @Expose @Range(min = -300 * 1000, max = 300 * 1000, step = 100)
  public final IntegerProperty subtitleDelay = new SimpleIntegerProperty() {
    @Override
    public void set(int newValue) {
      super.set(clamp(newValue, -300 * 1000, 300 * 1000));
    }
  };

  @Expose @Range(min = -10 * 1000, max = 10 * 1000, step = 100)
  public final IntegerProperty audioDelay = new SimpleIntegerProperty() {
    @Override
    public void set(int newValue) {
      super.set(clamp(newValue, -10 * 1000, 10 * 1000));
    }
  };

  @Expose(values = "availableSubtitles", stringConverter = SubtitleStringConverter.class)
  public final ObjectProperty<Subtitle> subtitle = new SimpleObjectProperty<>();
  public final ObservableList<Subtitle> availableSubtitles;

  @Expose(values = "availableAudioTracks", stringConverter = AudioTrackStringConverter.class)
  public final ObjectProperty<AudioTrack> audioTrack = new SimpleObjectProperty<>();
  public final ObservableList<AudioTrack> availableAudioTracks;

  private final Player player;

  @Inject
  public PlayerPresentation(Player player) {
    this.player = player;

    position.bindBidirectional(player.positionProperty());
    paused.bindBidirectional(player.pausedProperty());
    muted.bindBidirectional(player.mutedProperty());
    volume.bindBidirectional(player.volumeProperty());
    brightness.bindBidirectional(player.brightnessProperty());
    rate.bindBidirectional(player.rateProperty());
    subtitleDelay.bindBidirectional(player.subtitleDelayProperty());
    subtitle.bindBidirectional(player.subtitleProperty());
    audioDelay.bindBidirectional(player.audioDelayProperty());
    audioTrack.bindBidirectional(player.audioTrackProperty());

    availableSubtitles = player.getSubtitles();
    availableAudioTracks = player.getAudioTracks();
  }

  public void play(String uri, long positionMillis) {
    player.play(uri, positionMillis);
  }

  public void stop() {
    player.stop();
  }

  public void showSubtitle(Path path) {
    player.showSubtitle(path);
  }

  public Player getPlayer() {
    return player;
  }

  private int clamp(int value, int min, int max) {
    return value < min ? min :
           value > max ? max : value;
  }

  private long clamp(long value, long min, long max) {
    return value < min ? min :
           value > max ? max : value;
  }

  private float clamp(float value, float min, float max) {
    return value < min ? min :
           value > max ? max : value;
  }
}
