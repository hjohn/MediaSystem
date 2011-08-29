package hs.mediasystem.players.mplayer;

import hs.mediasystem.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MPlayerPlayer implements Player {
  private static final Pattern GET_PROPERTY_PATTERN = Pattern.compile("ANS_[a-zA-Z_]+=(.*)");
  private static final Pattern SUB_LOAD_RESPONSE = Pattern.compile("SUB: Added subtitle file \\(([0-9]+)\\):.*");
  
  private final PrintStream commandStream;
  private final LineNumberReader resultStream;
  
  private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
  
  private boolean isPlaying;
  
  public MPlayerPlayer(Path mplayerPath) {
    try {
      final String[] commands = new String[] {
          mplayerPath.toString(),
  //        "-nokeepaspect",
  //        "-geometry", "0:0",
  //        "-colorkey", "0x101010",
          "-fs",
          "-vo", "direct3d",   // gl2 only does overlay correctly when spread over 2 monitors
          "-idle",
          "-slave",
          "-quiet",
          "-fixed-vo",
          "-noar",
          "-noconsolecontrols",
          "-nomouseinput",
          "-nolirc",
          "-nojoystick",
          "-msglevel", "statusline=6:global=6"
          
       //   "-ontop"
          
      };
  
      final Process process = Runtime.getRuntime().exec(commands);
            
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
      String result = queue.take();
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
  public void play(Path path) {
    sendCommand("loadfile \"" + path.toString().replaceAll("\\\\", "\\\\\\\\") + "\"");
  }

  @Override
  public void pause() {
    sendCommand("pause");
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

  @Override
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
  }

  @Override
  public void showSubtitle(String fileName) {
    String result = sendCommandAndGetResult("sub_load " + fileName);
    
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
  public boolean isMute() {
    try {
      return getProperty("mute").equals("yes");
    }
    catch(MPlayerIllegalStateException e) {
      return false;
    }
  }

  @Override
  public void setMute(boolean mute) {
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
}
