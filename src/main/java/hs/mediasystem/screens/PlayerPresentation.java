package hs.mediasystem.screens;

import java.nio.file.Path;
import java.util.List;

import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;

public class PlayerPresentation {
  private final Player player;

  public PlayerPresentation(Player player) {
    this.player = player;
  }

  public void play(String uri) {
    player.play(uri);
  }

  public void stop() {
    player.stop();
  }

  public void pause() {
    player.pause();
  }

  public void move(int ms) {
    long position = player.getPosition();
    long length = player.getLength();
    long newPosition = position + ms;

    System.out.println("Position = " + position + "; length = " + length + "; np = " + newPosition);

    if(newPosition > length - 5000) {
      newPosition = length - 5000;
    }
    if(newPosition < 0) {
      newPosition = 0;
    }

    if(Math.abs(newPosition - position) > 5000) {
      player.setPosition(newPosition);
    }
  }

  public void mute() {
    player.setMute(!player.isMute());
  }

  public void changeVolume(int volumeDiff) {
    int volume = player.getVolume() + volumeDiff;

    if(volume > 100) {
      volume = 100;
    }
    if(volume < 0) {
      volume = 0;
    }

    player.setVolume(volume);
  }

  public void changeBrightness(float brightnessDiff) {
    float brightness = player.getBrightness() + brightnessDiff;

    if(brightness > 2.0) {
      brightness = 2.0f;
    }
    else if(brightness < 0.0) {
      brightness = 0.0f;
    }

    player.setBrightness(brightness);
  }

  public void changeSubtitleDelay(int msDelayDiff) {
    int delay = player.getSubtitleDelay() + msDelayDiff;

    player.setSubtitleDelay(delay);
  }

  public int getVolume() {
    return player.getVolume();
  }

  public long getPosition() {
    return player.getPosition();
  }

  public long getLength() {
    return player.getLength();
  }

  /**
   * Returns the current subtitle.  Never returns <code>null</code> but will return a special Subtitle
   * instance for when subtitles are unavailable or disabled.
   *
   * @return the current subtitle
   */
  public Subtitle nextSubtitle() {
    List<Subtitle> subtitles = player.getSubtitles();

    Subtitle currentSubtitle = player.getSubtitle();
    int index = subtitles.indexOf(currentSubtitle) + 1;

    if(index >= subtitles.size()) {
      index = 0;
    }

    player.setSubtitle(subtitles.get(index));

    return subtitles.get(index);
  }

  public void showSubtitle(Path path) {
    player.showSubtitle(path);
  }

  public void setScreen(int screenNumber) {
    player.setScreen(screenNumber);
  }

  public Player getPlayer() {
    return player;
  }
}
