package hs.mediasystem.players.vlc;

import hs.mediasystem.framework.Player;

import java.nio.file.Path;

import javax.swing.JFrame;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VLCPlayer implements Player {
  private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
  private final EmbeddedMediaPlayer mediaPlayer;
  private final JFrame frame;
  
  public VLCPlayer() {
    mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
    
    frame = new JFrame();
    frame.setUndecorated(true);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setContentPane(mediaPlayerComponent);
    frame.setVisible(true);

    mediaPlayer = mediaPlayerComponent.getMediaPlayer();
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
