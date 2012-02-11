package hs.mediasystem.players.vlc;

import hs.mediasystem.beans.Accessor;
import hs.mediasystem.beans.BeanAccessor;
import hs.mediasystem.beans.BeanFloatProperty;
import hs.mediasystem.beans.BeanObjectProperty;
import hs.mediasystem.beans.ComplexIntegerProperty;
import hs.mediasystem.beans.UpdatableLongProperty;
import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.TrackDescription;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VLCPlayer implements Player {
  private final EmbeddedMediaPlayer mediaPlayer;
  private final Frame frame;

  public VLCPlayer(GraphicsDevice device, String... args) {
    MediaPlayerFactory factory = new MediaPlayerFactory(args);

    mediaPlayer = factory.newEmbeddedMediaPlayer();

    Canvas canvas = new Canvas();

    frame = new Frame(device.getDefaultConfiguration());

    frame.setLayout(new BorderLayout());
    frame.setUndecorated(true);
    frame.setExtendedState(Frame.MAXIMIZED_BOTH);
//    device.setFullScreenWindow(frame);

    frame.add(canvas, BorderLayout.CENTER);
    frame.setBackground(new Color(0, 0, 0));
    frame.setVisible(true);

    mediaPlayer.setVideoSurface(factory.newVideoSurface(canvas));
    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      private AtomicInteger ignoreFinish = new AtomicInteger();

      @Override
      public void timeChanged(MediaPlayer mediaPlayer, final long newTime) {
        position.update(newTime);
      }

      @Override
      public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
        length.update(newLength);
      }

      @Override
      public void newMedia(MediaPlayer mediaPlayer) {
        System.out.println("[FINE] VLCPlayer: Event[newMedia]");
        updateSubtitles();
      }

      @Override
      public void mediaChanged(MediaPlayer mediaPlayer) {
        System.out.println("[FINE] VLCPlayer: Event[mediaChanged]");
      }

      @Override
      public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
        System.out.println("[FINE] VLCPlayer: Event[mediaMetaChanged]: " + metaType);
      }

      @Override
      public void mediaStateChanged(MediaPlayer mediaPlayer, int newState) {
        System.out.println("[FINE] VLCPlayer: Event[mediaStateChanged]: " + newState);
      }

      @Override
      public void mediaParsedChanged(MediaPlayer mediaPlayer, int parsed) {
        System.out.println("[FINE] VLCPlayer: Event[mediaParsedChanged]: " + parsed);
        if(parsed == 1) {
          updateSubtitles();
        }
      }

      @Override
      public void mediaSubItemAdded(MediaPlayer mediaPlayer, libvlc_media_t subItem) {
        ignoreFinish.incrementAndGet();
        int i = 1;

        System.out.println("VLCPlayer: mediaSubItemAdded: " + subItem.toString());

        for(TrackDescription desc : mediaPlayer.getTitleDescriptions()) {
          System.out.println(i++ + " : " + desc.description());
        }
      }

      @Override
      public void finished(MediaPlayer mediaPlayer) {
        int index = mediaPlayer.subItemIndex();
        System.out.println(index);

        List<String> subItems = mediaPlayer.subItems();

        if(index < subItems.size()) {
          System.out.println("Finished: " + subItems.get(index));
        }

        if(ignoreFinish.get() == 0) {  // TODO Fix/Remove this code
          // finishedNotifier.notifyListeners("FINISH");
          System.out.println("VLCPlayer: Finished");
        }
        else {
          ignoreFinish.decrementAndGet();
          System.out.println("VLCPlayer: Adding more media");
//          mediaPlayer.playMedia(uri);
        }
      }
    });

    length = new UpdatableLongProperty();
    position = new UpdatableLongProperty() {
      @Override
      public void set(long v) {
        long value = v < 1 ? 1 : v;  // TODO workaround for VLC bug with MKV files (it doesn't like skipping to 0)
        mediaPlayer.setTime(value);
        super.set(value);
      }
    };
    volume = new ComplexIntegerProperty(new BeanAccessor<Integer>(mediaPlayer, "volume"));
    audioDelay = new ComplexIntegerProperty(new Accessor<Integer>() {
      @Override
      public void write(Integer value) {
        mediaPlayer.setAudioDelay(value * 1000L);
      }

      @Override
      public Integer read() {
        return (int)(mediaPlayer.getAudioDelay() / 1000);
      }
    });
    rate = new BeanFloatProperty(mediaPlayer, "rate");
    brightness = new BeanFloatProperty(mediaPlayer, "brightness");
  }

  public AudioTrack getAudioTrackInternal() {
    int index = mediaPlayer.getAudioTrack();

    if(index == -1) {
      return AudioTrack.NO_AUDIO_TRACK;
    }
    return getAudioTracks().get(index);
  }

  public void setAudioTrackInternal(AudioTrack audioTrack) {
    mediaPlayer.setAudioTrack(getAudioTracks().indexOf(audioTrack));
  }

  public Subtitle getSubtitleInternal() {
    int index = mediaPlayer.getSpu();

    return index == -1 ? Subtitle.DISABLED : getSubtitles().get(index);
  }

  public void setSubtitleInternal(Subtitle subtitle) {
    System.out.println("[FINE] VLCPlayer.setSubtitleInternal() - Subtitles available: " + getSubtitles());
    System.out.println("[FINE] VLCPlayer.setSubtitleInternal() - Setting subtitle to: " + subtitle + ", index = " + getSubtitles().indexOf(subtitle));
    mediaPlayer.setSpu(getSubtitles().indexOf(subtitle));
  }

  private final ObservableList<Subtitle> subtitles = FXCollections.observableArrayList(Subtitle.DISABLED);

  @Override
  public ObservableList<Subtitle> getSubtitles() {
    updateSubtitles();
    return FXCollections.unmodifiableObservableList(subtitles);
  }

  private final List<AudioTrack> audioTracks = new ArrayList<>();

  @Override
  public ObservableList<AudioTrack> getAudioTracks() {
    if(audioTracks.isEmpty()) {
      for(TrackDescription description : mediaPlayer.getAudioDescriptions()) {
        audioTracks.add(new AudioTrack(description.id(), description.description()));
      }
    }

    return FXCollections.observableArrayList(audioTracks.isEmpty() ? NO_AUDIO_TRACKS : audioTracks);
  }

  private static final List<AudioTrack> NO_AUDIO_TRACKS = new ArrayList<>();

  @Override
  public void play(String uri) {
    mediaPlayer.setRepeat(true);
    mediaPlayer.setPlaySubItems(true);
    mediaPlayer.playMedia(uri);

    System.out.println("[FINE] Playing: " + uri);
  }

  @Override
  public void pause() {
    mediaPlayer.pause();
  }

  @Override
  public boolean isPlaying() {
    return mediaPlayer.isPlaying();
  }

  @Override
  public long getLength() {
    return mediaPlayer.getLength();
  }

  @Override
  public void stop() {
    mediaPlayer.stop();
  }

  @Override
  public void dispose() {
    mediaPlayer.release();
    frame.dispose();
  }

  @Override
  public void showSubtitle(Path path) {
    System.out.println("[INFO] VLCPlayer.showSubtitle: path = " + path.toString());
    mediaPlayer.setSubTitleFile(path.toString());
  }

  private void updateSubtitles() {
    if(subtitles.size() > 1) {
      subtitles.subList(1, subtitles.size()).clear();
    }

    for(TrackDescription spuDescription : mediaPlayer.getSpuDescriptions()) {
      if(spuDescription.id() > 0) {
        subtitles.add(new Subtitle(spuDescription.id(), spuDescription.description()));
      }
    }
  }

  private final UpdatableLongProperty length;
  @Override public ReadOnlyLongProperty lengthProperty() { return length; }

  private final UpdatableLongProperty position;
  @Override public long getPosition() { return position.get(); }
  @Override public void setPosition(long position) { this.position.set(position); }
  @Override public LongProperty positionProperty() { return position; }

  private final IntegerProperty volume;
  @Override public int getVolume() { return volume.get(); }
  @Override public void setVolume(int volume) { this.volume.set(volume); }
  @Override public IntegerProperty volumeProperty() { return volume; }

  private final IntegerProperty audioDelay;
  @Override public int getAudioDelay() { return audioDelay.get(); }
  @Override public void setAudioDelay(int audioDelay) { this.audioDelay.set(audioDelay); }
  @Override public IntegerProperty audioDelayProperty() { return audioDelay; }

  private final FloatProperty rate;
  @Override public float getRate() { return rate.get(); }
  @Override public void setRate(float rate) { this.rate.set(rate); }
  @Override public FloatProperty rateProperty() { return rate; }

  private final FloatProperty brightness;
  @Override public float getBrightness() { return brightness.get(); }
  @Override public void setBrightness(float brightness) { this.brightness.set(brightness); }
  @Override public FloatProperty brightnessProperty() { return brightness; }

  private final BeanObjectProperty<Subtitle> subtitle = new BeanObjectProperty<>(this, "subtitleInternal");
  @Override public Subtitle getSubtitle() { return subtitle.get(); }
  @Override public void setSubtitle(Subtitle subtitle) { this.subtitle.set(subtitle); }
  @Override public ObjectProperty<Subtitle> subtitleProperty() { return subtitle; }

  private final ObjectProperty<AudioTrack> audioTrack = new BeanObjectProperty<>(this, "audioTrackInternal");
  @Override public AudioTrack getAudioTrack() { return audioTrack.get(); }
  @Override public void setAudioTrack(AudioTrack audioTrack) { this.audioTrack.set(audioTrack); }
  @Override public ObjectProperty<AudioTrack> audioTrackProperty() { return audioTrack; }

  @Override
  public boolean isMute() {
    return mediaPlayer.isMute();
  }

  @Override
  public void setMute(boolean mute) {
    mediaPlayer.mute(mute);
  }

  @Override
  public int getSubtitleDelay() {
    return 0;
  }

  @Override
  public void setSubtitleDelay(int delay) {
    throw new UnsupportedOperationException("Method not implemented");
  }
}
