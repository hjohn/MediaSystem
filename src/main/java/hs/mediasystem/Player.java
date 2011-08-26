package hs.mediasystem;

import hs.mediasystem.screens.movie.MovieElement;

public interface Player {

  void play(MovieElement item);

  void pause();

  /**
   * Returns the position of the stream in milliseconds.
   * 
   * @return the position of the stream in milliseconds
   */
  long getPosition();

  void setPosition(long position);

  boolean isPlaying();

  /**
   * Returns the length of the stream in milliseconds.
   * 
   * @return the length of the stream in milliseconds
   */
  long getLength();

  void stop();

  void dispose();

  void showSubtitle(String fileName);
  
  int getVolume();
  void setVolume(int volume);
  
  boolean isMute();
  void setMute(boolean mute);

  int getSubtitleDelay();
  void setSubtitleDelay(int delay);
}
