package hs.mediasystem.util;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

public class DebugConsole {
  private static final Map<String, CommandCallback> COMMANDS = new HashMap<>();
  private static LineNumberReader reader;

  public static void initialize() {
    if(reader == null) {
      reader = new LineNumberReader(new InputStreamReader(System.in));

      new Thread("DebugConsole") {
        {
          setDaemon(true);
        }

        @Override
        public void run() {
          try {
            for(;;) {
              String line = reader.readLine();

              if(line == null) {
                System.out.println("[WARN] Debug Console exiting because input stream was closed");
                break;
              }

              int firstSpace = line.indexOf(' ');
              String command = firstSpace == -1 ? line : line.substring(0, firstSpace);
              String parameters = firstSpace == -1 ? "" : line.substring(firstSpace + 1);

              CommandCallback commandCallback = COMMANDS.get(command);

              if(commandCallback == null) {
                System.out.println("Unknown command '" + command + "'.");
              }
              else {
                String result = commandCallback.execute(command, parameters);

                if(result != null) {
                  System.out.println(command + ": " + result);
                }
              }
            }
          }
          catch(Exception e) {
            throw new RuntimeException(e);
          }
        }
      }.start();
    }
  }

  public static void addCommand(String name, CommandCallback callback) {
    initialize();
    COMMANDS.put(name, callback);
  }

  public interface CommandCallback {
    String execute(String name, String parameters);
  }
}
