package hs.mediasystem.ext.player.vlc;

import hs.mediasystem.beans.Accessor;
import hs.mediasystem.beans.BeanAccessor;
import hs.mediasystem.beans.BeanBooleanProperty;
import hs.mediasystem.beans.BeanFloatProperty;
import hs.mediasystem.beans.BeanIntegerProperty;
import hs.mediasystem.beans.BeanObjectProperty;
import hs.mediasystem.beans.UpdatableLongProperty;
import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.PlayerEvent;
import hs.mediasystem.framework.player.PlayerEvent.Type;
import hs.mediasystem.framework.player.Subtitle;
import hs.mediasystem.util.Events;

import java.awt.Canvas;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.TrackDescription;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import com.sun.jna.Memory;

public class VLCPlayer implements Player {
  public enum Mode {
    SEPERATE_WINDOW, CANVAS
  }

  private static final int WIDTH = 1920;
  private static final int HEIGHT = 1080;

  private final MediaPlayer mediaPlayer;
  private final Object canvas;

  public VLCPlayer(Mode mode, String... args) {
    List<String> arguments = new ArrayList<>(Arrays.asList(args));

    arguments.add("--no-plugins-cache");
    arguments.add("--no-snapshot-preview");
    arguments.add("--input-fast-seek");
    arguments.add("--no-video-title-show");
    arguments.add("--network-caching");
    arguments.add("3000");
    arguments.add("--quiet");
    arguments.add("--quiet-synchro");
    arguments.add("--intf");
    arguments.add("dummy");

    MediaPlayerFactory factory = new MediaPlayerFactory(arguments);

    if(mode == Mode.SEPERATE_WINDOW) {
      EmbeddedMediaPlayer mp = factory.newEmbeddedMediaPlayer();

      Canvas canvas = new Canvas();

      mp.setVideoSurface(factory.newVideoSurface(canvas));

      this.canvas = canvas;
      this.mediaPlayer = mp;
    }
    else {
      javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(WIDTH, HEIGHT);

      final PixelWriter pixelWriter = canvas.getGraphicsContext2D().getPixelWriter();
      final WritablePixelFormat<ByteBuffer> byteBgraInstance = PixelFormat.getByteBgraPreInstance();

      DirectMediaPlayer mp = factory.newDirectMediaPlayer("RV32", WIDTH, HEIGHT, WIDTH * 4, new RenderCallback() {
        @Override
        public void display(Memory memory) {
          ByteBuffer byteBuffer = memory.getByteBuffer(0, memory.size());
          pixelWriter.setPixels(0, 0, WIDTH, HEIGHT, byteBgraInstance, byteBuffer, WIDTH * 4);
        }
      });

      this.canvas = canvas;
      this.mediaPlayer = mp;
    }

    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      private AtomicInteger ignoreFinish = new AtomicInteger();
      private boolean videoAdjusted;

      @Override
      public void timeChanged(MediaPlayer mediaPlayer, final long newTime) {
        position.update(newTime);
        if(!videoAdjusted) {
          mediaPlayer.setAdjustVideo(true);
        }
      }

      @Override
      public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
        length.update(newLength);
      }

      @Override
      public void newMedia(MediaPlayer mediaPlayer) {
        System.out.println("[FINE] VLCPlayer: Event[newMedia]");
        updateSubtitles();
        videoAdjusted = false;
      }

      @Override
      public void mediaChanged(MediaPlayer mediaPlayer) {
        System.out.println("[FINE] VLCPlayer: Event[mediaChanged]");
      }

      @Override
      public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
        // 12 = NowPlaying(?), 13 = Publisher(?), 15 = ArtworkURL(?)
      }

      @Override
      public void mediaStateChanged(MediaPlayer mediaPlayer, int newState) {
        // IDLE/CLOSE=0, OPENING=1, BUFFERING=2, PLAYING=3, PAUSED=4, STOPPING=5, ENDED=6, ERROR=7
        System.out.println("[FINE] VLCPlayer: Event[mediaStateChanged]: " + newState);

        pausedProperty.update(newState == 4);
      }

      @Override
      public void mediaParsedChanged(MediaPlayer mediaPlayer, int parsed) {
        System.out.println("[FINE] VLCPlayer: Event[mediaParsedChanged]: " + parsed);
        if(parsed == 1) {
          updateSubtitles();
          audioTrack.update();
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
      public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
        System.out.println("VLCPlayer: videoOutput");
        mediaPlayer.setVolume(mediaPlayer.getVolume());
      }

      @Override
      public void finished(MediaPlayer mediaPlayer) {
        if(ignoreFinish.get() == 0) {
          System.out.println("VLCPlayer: Finished");
          Events.dispatchEvent(onPlayerEvent, new PlayerEvent(Type.FINISHED), null);
        }
        else {
          int index = mediaPlayer.subItemIndex();

          List<String> subItems = mediaPlayer.subItems();

          if(index>= 0 && index < subItems.size()) {
            System.out.println("Finished: " + subItems.get(index));
          }
          else {
            index = 0;
          }

          ignoreFinish.decrementAndGet();
          System.out.println("VLCPlayer: Adding more media");
          mediaPlayer.playMedia(subItems.get(index));
        }
      }
    });

    length = new UpdatableLongProperty();
    position = new UpdatableLongProperty() {
      @Override
      public void set(long value) {
        mediaPlayer.setTime(value);
        super.set(value);
      }
    };
    volume = new BeanIntegerProperty(new BeanAccessor<Integer>(mediaPlayer, "volume"));
    audioDelay = new BeanIntegerProperty(new Accessor<Integer>() {
      @Override
      public void write(Integer value) {
        mediaPlayer.setAudioDelay(value * 1000L);
      }

      @Override
      public Integer read() {
        return (int)(mediaPlayer.getAudioDelay() / 1000);
      }
    });
    subtitleDelay = new BeanIntegerProperty(new Accessor<Integer>() {
      @Override
      public void write(Integer value) {
        mediaPlayer.setSpuDelay(-value * 1000L);
      }

      @Override
      public Integer read() {
        return (int)(-mediaPlayer.getSpuDelay() / 1000);
      }
    });
    rate = new BeanFloatProperty(mediaPlayer, "rate");
    brightness = new BeanFloatProperty(mediaPlayer, "brightness");
    mutedProperty = new BeanBooleanProperty(new Accessor<Boolean>() {
      @Override
      public Boolean read() {
        return mediaPlayer.isMute();
      }

      @Override
      public void write(Boolean value) {
        mediaPlayer.mute(value);
      }
    });
    pausedProperty = new BeanBooleanProperty(new Accessor<Boolean>() {
      @Override
      public Boolean read() {
        return false;
      }

      @Override
      public void write(Boolean value) {
        mediaPlayer.setPause(value);
      }
    });
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
  public void play(String uri, long positionInMillis) {
    position.update(0);
    audioDelay.update();
    audioTrack.update();
    brightness.update();
    rate.update();
    subtitle.update();
    subtitleDelay.update();
    volume.update();

    List<String> arguments = new ArrayList<>();

    if(positionInMillis > 0) {
      arguments.add("start-time=" + positionInMillis / 1000);
    }

    mediaPlayer.setRepeat(false);
    mediaPlayer.setPlaySubItems(true);
    mediaPlayer.playMedia(uri, arguments.toArray(new String[arguments.size()]));

    System.out.println("[FINE] Playing: " + uri);
  }

  @Override
  public void stop() {
    mediaPlayer.stop();
  }

  @Override
  public void dispose() {
    mediaPlayer.release();
  }

  @Override
  public Object getDisplayComponent() {
    return canvas;
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
  @Override public long getLength() { return length.get(); }
  @Override public ReadOnlyLongProperty lengthProperty() { return length; }

  private final UpdatableLongProperty position;
  @Override public long getPosition() { return position.get(); }
  @Override public void setPosition(long position) { this.position.set(position); }
  @Override public LongProperty positionProperty() { return position; }

  private final BeanIntegerProperty volume;
  @Override public int getVolume() { return volume.get(); }
  @Override public void setVolume(int volume) { this.volume.set(volume); }
  @Override public IntegerProperty volumeProperty() { return volume; }

  private final BeanIntegerProperty audioDelay;
  @Override public int getAudioDelay() { return audioDelay.get(); }
  @Override public void setAudioDelay(int audioDelay) { this.audioDelay.set(audioDelay); }
  @Override public IntegerProperty audioDelayProperty() { return audioDelay; }

  private final BeanFloatProperty rate;
  @Override public float getRate() { return rate.get(); }
  @Override public void setRate(float rate) { this.rate.set(rate); }
  @Override public FloatProperty rateProperty() { return rate; }

  private final BeanFloatProperty brightness;
  @Override public float getBrightness() { return brightness.get(); }
  @Override public void setBrightness(float brightness) { this.brightness.set(brightness); }
  @Override public FloatProperty brightnessProperty() { return brightness; }

  private final BeanObjectProperty<Subtitle> subtitle = new BeanObjectProperty<>(this, "subtitleInternal");
  @Override public Subtitle getSubtitle() { return subtitle.get(); }
  @Override public void setSubtitle(Subtitle subtitle) { this.subtitle.set(subtitle); }
  @Override public ObjectProperty<Subtitle> subtitleProperty() { return subtitle; }

  private final BeanObjectProperty<AudioTrack> audioTrack = new BeanObjectProperty<>(this, "audioTrackInternal");
  @Override public AudioTrack getAudioTrack() { return audioTrack.get(); }
  @Override public void setAudioTrack(AudioTrack audioTrack) { this.audioTrack.set(audioTrack); }
  @Override public ObjectProperty<AudioTrack> audioTrackProperty() { return audioTrack; }

  private final BeanIntegerProperty subtitleDelay;
  @Override public int getSubtitleDelay() { return subtitleDelay.get(); }
  @Override public void setSubtitleDelay(int subtitleDelay) { this.subtitleDelay.set(subtitleDelay); }
  @Override public IntegerProperty subtitleDelayProperty() { return subtitleDelay; }

  private final BooleanProperty mutedProperty;
  @Override public boolean isMuted() { return mutedProperty.get(); }
  @Override public void setMuted(boolean muted) { mutedProperty.set(muted); }
  @Override public BooleanProperty mutedProperty() { return mutedProperty; }

  private final BeanBooleanProperty pausedProperty;
  @Override public boolean isPaused() { return pausedProperty.get(); }
  @Override public void setPaused(boolean paused) { pausedProperty.set(paused); }
  @Override public BooleanProperty pausedProperty() { return pausedProperty; }

  private final ObjectProperty<EventHandler<PlayerEvent>> onPlayerEvent = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<PlayerEvent>> onPlayerEvent() { return onPlayerEvent; }
}
