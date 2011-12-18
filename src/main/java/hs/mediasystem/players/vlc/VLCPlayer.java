package hs.mediasystem.players.vlc;

import hs.mediasystem.framework.Player;
import hs.mediasystem.framework.Subtitle;
import hs.models.events.ListenerList;
import hs.models.events.Notifier;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.TrackDescription;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VLCPlayer implements Player {
  private final EmbeddedMediaPlayer mediaPlayer;
  private final Frame frame;
  
  private final Notifier<String> finishedNotifier = new Notifier<String>();

  @Override
  public ListenerList<String> onFinished() {
    return finishedNotifier.getListenerList();
  }
  
  public VLCPlayer(GraphicsDevice device) {
    String[] libvlcArgs = {"-V", "direct3d"};  // opengl direct3d
    MediaPlayerFactory factory = new MediaPlayerFactory(libvlcArgs);

    mediaPlayer = factory.newEmbeddedMediaPlayer();
    
    Canvas canvas = new Canvas();
    
    frame = new Frame(device.getDefaultConfiguration());
    
    
    frame.setLayout(new BorderLayout());
    frame.setUndecorated(true);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//    device.setFullScreenWindow(frame);
    
    frame.add(canvas, BorderLayout.CENTER);
    frame.setBackground(new Color(0, 0, 0));
    frame.setVisible(true);
    
    mediaPlayer.setVideoSurface(factory.newVideoSurface(canvas));
    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      private AtomicInteger ignoreFinish = new AtomicInteger();
      
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
        
        if(ignoreFinish.get() == 0) {
          // finishedNotifier.notifyListeners("FINISH");
        }
        else {
          ignoreFinish.decrementAndGet();
          System.out.println("VLCPlayer: Adding more media");
//          mediaPlayer.playMedia(uri);
        }
      }
    });
  }
  
  public Subtitle getSubtitleInternal() {
    int index = mediaPlayer.getSpu();
    
    return index == -1 ? Subtitle.DISABLED : getSubtitles().get(index);
//    
//    for(Subtitle subtitle : getSubtitles()) {
//      if(subtitle.getId() == id) {
//        System.out.println("getSubtitle(): returning " + id + "; " + subtitle.getDescription());
//        return subtitle;
//      }
//    }
//    
//    return Subtitle.DISABLED;
  }

  private final List<Subtitle> subtitles = new ArrayList<>();

  @Override
  public List<Subtitle> getSubtitles() {
    if(subtitles.isEmpty()) {
      for(TrackDescription spuDescription : mediaPlayer.getSpuDescriptions()) {
        subtitles.add(new Subtitle(spuDescription.id(), spuDescription.description()));
      }
      
      if(!subtitles.isEmpty()) {
        subtitles.remove(0);
        subtitles.add(0, Subtitle.DISABLED);
      }
    }
    
    return subtitles.isEmpty() ? NO_SUBTITLES : subtitles;
  }

  private static final List<Subtitle> NO_SUBTITLES = new ArrayList<Subtitle>() {{
    add(Subtitle.DISABLED);
  }};
  
  private final ObjectProperty<Subtitle> subtitle = new SimpleObjectProperty<Subtitle>() {
    private boolean initialized;
    
    @Override
    public Subtitle get() {
      if(!initialized) {
        initialized = true;
        set(getSubtitleInternal());
      }
      
      return super.get();
    }
    
    @Override
    public void set(Subtitle value) {
      super.set(value);
      mediaPlayer.setSpu(getSubtitles().indexOf(value));
    }
  };
  
  @Override
  public Subtitle getSubtitle() {
    return subtitle.get();
  }
  
  @Override
  public void setSubtitle(Subtitle subtitle) {
    this.subtitle.set(subtitle);
  }
  
  @Override
  public ObjectProperty<Subtitle> subtitleProperty() {
    return subtitle;
  }
  
  @Override
  public void play(String uri) {
    subtitles.clear();
    mediaPlayer.setRepeat(true);
    mediaPlayer.setPlaySubItems(true);
    mediaPlayer.playMedia(uri);
    
    System.out.println("[FINE] Playing: " + uri);
    
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        System.out.println("[FINE] Found " + mediaPlayer.getSpuCount() + " subtitle(s)");
        for(TrackDescription spuDescription : mediaPlayer.getSpuDescriptions()) {
          System.out.println("[FINE] Subtitle " + spuDescription.id() + ": " + spuDescription.description());
        }
      }
    }.start();
  }

  @Override
  public void pause() {
    mediaPlayer.pause();
  }

  @Override
  public long getPosition() {
    return mediaPlayer.getTime();
  }

  @Override
  public void setPosition(long position) {
    mediaPlayer.setTime(position);
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
  public void showSubtitle(String fileName) {
    mediaPlayer.setSubTitleFile(fileName);
  }

  private final IntegerProperty volume = new SimpleIntegerProperty(100) {
    @Override
    public void set(int value) {
      super.set(value);
      mediaPlayer.setVolume(value);
    }
  };
  
  @Override
  public int getVolume() {
    return volume.get();
  }

  @Override
  public void setVolume(int volume) {
    this.volume.set(volume);
  }
  
  @Override
  public IntegerProperty volumeProperty() {
    return volume;
  }

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

  @Override
  public float getBrightness() {
    return mediaPlayer.getBrightness();
  }

  @Override
  public void setBrightness(float brightness) {
    mediaPlayer.setBrightness(brightness);
  }
}
