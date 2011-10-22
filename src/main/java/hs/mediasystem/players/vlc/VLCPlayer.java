package hs.mediasystem.players.vlc;

import hs.mediasystem.framework.Player;
import hs.models.events.ListenerList;
import hs.models.events.Notifier;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.nio.file.Path;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VLCPlayer implements Player {
  private final EmbeddedMediaPlayer mediaPlayer;
  private final Frame frame;
  
  private final Notifier<String> finishedNotifier = new Notifier<String>();

  @Override
  public ListenerList<String> onFinished() {
    return finishedNotifier.getListenerList();
  }
  
  public VLCPlayer() {
    String[] libvlcArgs = {"-V", "directx"};  // opengl direct3d
    MediaPlayerFactory factory = new MediaPlayerFactory(libvlcArgs);

    mediaPlayer = factory.newEmbeddedMediaPlayer();
    
    Canvas canvas = new Canvas();
    
    frame = new Frame();
    
    frame.setLayout(new BorderLayout());
    frame.setUndecorated(true);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.add(canvas, BorderLayout.CENTER);
    frame.setBackground(new Color(0, 0, 0));
    frame.setVisible(true);
    
    mediaPlayer.setVideoSurface(factory.newVideoSurface(canvas));
    mediaPlayer.addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
      @Override
      public void finished(MediaPlayer mediaPlayer) {
        finishedNotifier.notifyListeners("FINISH");
      }
    });
  }
  
  @Override
  public void play(Path path) {
    mediaPlayer.playMedia(path.toString());
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

  @Override
  public int getVolume() {
    return mediaPlayer.getVolume();
  }

  @Override
  public void setVolume(int volume) {
    mediaPlayer.setVolume(volume);
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
}
