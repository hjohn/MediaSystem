package hs.mediasystem.framework;

import hs.models.events.ListenerList;

public interface Player {

  void play(String uri);

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
  
  /**
   * Returns the current volume in percent.
   * 
   * @return the current volume in percent
   */
  int getVolume();
  void setVolume(int volume);
  
  boolean isMute();
  void setMute(boolean mute);

  /**
   * Returns the current subtitle delay in milliseconds.
   * 
   * @return the current subtitle delay in milliseconds
   */
  int getSubtitleDelay();
  void setSubtitleDelay(int delay);
  
  /**
   * Returns the current brightness in a range of 0.0 to 2.0.
   * 
   * @return the current brightness
   */
  float getBrightness();
  void setBrightness(float brightness);
  
  ListenerList<String> onFinished();
}
