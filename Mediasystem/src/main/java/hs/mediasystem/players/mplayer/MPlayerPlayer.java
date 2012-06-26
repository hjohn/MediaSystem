package hs.mediasystem.players.mplayer;

import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.PlayerEvent;
import hs.mediasystem.framework.player.Subtitle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;

public class MPlayerPlayer implements Player {
  private static final Pattern GET_PROPERTY_PATTERN = Pattern.compile("ANS_[a-zA-Z_]+=(.*)");
  private static final Pattern SUB_LOAD_RESPONSE = Pattern.compile("SUB: Added subtitle file \\(([0-9]+)\\):.*");

  private final PrintStream commandStream;
  private final LineNumberReader resultStream;
  private final boolean useFrame;

  private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

  private Frame frame;
  private boolean isPlaying;

  public MPlayerPlayer(Path mplayerPath, boolean useFrame) {
    this.useFrame = useFrame;

    try {
      List<String> commands = new ArrayList<>();

      commands.add(mplayerPath.toString());

      if(useFrame) {
        frame = new Frame();
        frame.setUndecorated(true);
        frame.setBackground(Color.BLACK);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        commands.add("-wid");
        commands.add("" + getWindowId(frame));
      }
      else {
        commands.add("-fixed-vo");
      }

      commands.add("-cache");
      commands.add("8192");
      commands.add("-autosync");
      commands.add("30");
      commands.add("-ao");
      commands.add("dsound");
      commands.add("-fs");
      commands.add("-vo");
      commands.add("gl2");
      commands.add("-idle");
      commands.add("-slave");
      //commands.add("-quiet");
      commands.add("-noar");
      commands.add("-noconsolecontrols");
      commands.add("-nomouseinput");
      commands.add("-nolirc");
      commands.add("-nojoystick");
      commands.add("-msglevel");
      commands.add("statusline=6:global=6");

  //        "-nokeepaspect",
  //        "-geometry", "0:0",
  //        "-colorkey", "0x101010",
       //   "-ontop"

      final Process process = Runtime.getRuntime().exec(commands.toArray(new String[commands.size()]));

      commandStream = new PrintStream(process.getOutputStream());
      resultStream = new LineNumberReader(new InputStreamReader(process.getInputStream()));

      Thread thread = new Thread() {
        @Override
        public void run() {
          try {
            for(;;) {
              String line = resultStream.readLine();

              if(line == null) {
                break;
              }

              System.out.println("MPLAYER: [INPUT] " + line);

              if(line.matches("EOF code:.*")) {
                isPlaying = false;
              }
              else {
                queue.add(line);
              }
            }
          }
          catch(IOException e) {
            throw new RuntimeException(e);
          }

          System.out.println("Thread '" + this.getName() + "' exiting.");
        }
      };

      thread.setDaemon(true);
      thread.setName("MPlayer Input Handler");
      thread.start();
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }

//    sendCommand("loadfile \"d:/empty-gifanim2.gif\"");
//    sendCommand("pause");
  }

  private String getProperty(String propertyName) throws MPlayerIllegalStateException {
    String result = sendCommandAndGetResult("pausing_keep_force get_property " + propertyName);

    if(!result.startsWith("ANS_ERROR=")) {
      Matcher matcher = GET_PROPERTY_PATTERN.matcher(result);
      if(matcher.matches()) {
        return matcher.group(1);
      }
    }

    throw new MPlayerIllegalStateException();
  }

  private void setProperty(String propertyName, Object value) {
    sendCommand("pausing_keep_force set_property " + propertyName + " " + value);
  }

  private String sendCommandAndGetResult(String command, Object... parameters) {
    try {
      queue.clear();
      sendCommand(command, parameters);
      String result = queue.poll(100, TimeUnit.MILLISECONDS);
      if(result == null) {
        sendCommand(command, parameters);
        result = queue.poll(200, TimeUnit.MILLISECONDS);

        if(result == null) {
          throw new RuntimeException();  // TODO this apparently happens when sending commands to mplayer in quick succession...
        }
      }
      System.out.println("MPLAYER: --> " + result);
      return result;
    }
    catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void sendCommand(String command, Object... parameters) {
    String formattedCommand = String.format(command, parameters);
    System.out.println("MPLAYER: " + formattedCommand);
    commandStream.println(formattedCommand);
    commandStream.flush();
  }

  @Override
  public void play(String uri, long positionMillis) {
    sendCommand("loadfile \"" + uri.replaceAll("\\\\", "\\\\\\\\") + "\"");
  }

  @Override
  public long getPosition() {
    try {
      return (long)(Float.parseFloat(getProperty("time_pos")) * 1000);
    }
    catch(MPlayerIllegalStateException e) {
      return 0;
    }
  }

  @Override
  public void setPosition(long time) {
    sendCommand("seek %10.3f 2", (double)time / 1000);
  }

  public boolean isPlaying() {
    return isPlaying;
  }

  @Override
  public long getLength() {
    try {
      return (long)(Float.parseFloat(getProperty("length")) * 1000);
    }
    catch(MPlayerIllegalStateException e) {
      return 0;
    }
  }

  @Override
  public void stop() {
    sendCommand("stop");
  }

  @Override
  public void dispose() {
    sendCommand("quit");

    if(useFrame) {
      frame.dispose();
    }
  }

  @Override
  public void showSubtitle(Path path) {
    String result = sendCommandAndGetResult("sub_load " + path.toString());

    Matcher matcher = SUB_LOAD_RESPONSE.matcher(result);

    if(matcher.matches()) {
      sendCommand("sub_select " + matcher.group(1));
    }
  }

  private int cachedVolume = -1;
  private int subtitleDelay = 0;

  @Override
  public int getVolume() {
    if(cachedVolume == -1) {
      try {
        cachedVolume = (int)(Float.parseFloat(getProperty("volume")) + 0.5);
      }
      catch(MPlayerIllegalStateException e) {
        cachedVolume = 0;
      }
    }

    return cachedVolume;
  }

  @Override
  public void setVolume(int volume) {
    if(volume < 0 || volume > 100) {
      throw new IllegalArgumentException("parameter 'volume' must be in the range 0...100");
    }

    setProperty("volume", volume);
    sendCommand("osd_show_text \"volume: %d%%\"", volume);
    cachedVolume = volume;
  }

  @Override
  public boolean isMuted() {
    try {
      return getProperty("mute").equals("yes");
    }
    catch(MPlayerIllegalStateException e) {
      return false;
    }
  }

  @Override
  public void setMuted(boolean mute) {
    setProperty("mute", mute ? "1" : "0");
  }

  @Override
  public int getSubtitleDelay() {
    return subtitleDelay;
  }

  @Override
  public void setSubtitleDelay(int delay) {
    setProperty("subtitle_delay", ((float)delay) / 1000);
    sendCommand("osd_show_text \"subtitle delay: %4.1f s\"", ((float)delay) / 1000);
    subtitleDelay = delay;
  }

  @SuppressWarnings({"restriction", "deprecation"})
  private static long getWindowId(Window window) {
    if(window.getPeer() instanceof sun.awt.windows.WComponentPeer) {
      return ((sun.awt.windows.WComponentPeer)window.getPeer()).getHWnd();
    }

    throw new RuntimeException("unable to get wid, window.getPeer().getClass(): " + window.getPeer().getClass());
  }

  @Override
  public float getBrightness() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public void setBrightness(float brightness) {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public void setSubtitle(Subtitle subtitle) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Subtitle getSubtitle() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObservableList<Subtitle> getSubtitles() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntegerProperty volumeProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectProperty<Subtitle> subtitleProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public float getRate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setRate(float rate) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FloatProperty rateProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getAudioDelay() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAudioDelay(int rate) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntegerProperty audioDelayProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AudioTrack getAudioTrack() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setAudioTrack(AudioTrack audioTrack) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectProperty<AudioTrack> audioTrackProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public FloatProperty brightnessProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObservableList<AudioTrack> getAudioTracks() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LongProperty positionProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ReadOnlyLongProperty lengthProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntegerProperty subtitleDelayProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BooleanProperty mutedProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public BooleanProperty pausedProperty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isPaused() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPaused(boolean paused) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Component getDisplayComponent() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectProperty<EventHandler<PlayerEvent>> onPlayerEvent() {
    throw new UnsupportedOperationException();
  }
}
