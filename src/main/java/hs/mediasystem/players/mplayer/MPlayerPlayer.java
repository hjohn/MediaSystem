package hs.mediasystem.players.mplayer;

import hs.mediasystem.Player;
import hs.mediasystem.screens.movie.MovieElement;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MPlayerPlayer implements Player {
  private static final String MPLAYER = "D:/Dev/Workspaces/Personal/MPlayer/mplayer.exe";
  private static final Pattern GET_PROPERTY_PATTERN = Pattern.compile("ANS_[a-zA-Z_]+=(.*)");
  private static final Pattern SUB_LOAD_RESPONSE = Pattern.compile("SUB: Added subtitle file \\(([0-9]+)\\):.*");
  
  private final PrintStream commandStream;
  private final LineNumberReader resultStream;
  
  private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
  
  private boolean isPlaying;
  
  public MPlayerPlayer() {
    try {
      final String[] commands = new String[] {
          MPLAYER,
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
    String result = sendCommandAndGetResult("get_property " + propertyName);
    
    if(!result.startsWith("ANS_ERROR=")) {
      Matcher matcher = GET_PROPERTY_PATTERN.matcher(result);
      if(matcher.matches()) {
        return matcher.group(1);
      }
    }
    
    throw new MPlayerIllegalStateException();
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
  public void play(MovieElement item) {
    sendCommand("loadfile \"" + item.getPath().toString() + "\"");
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
}
