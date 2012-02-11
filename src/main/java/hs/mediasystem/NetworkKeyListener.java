package hs.mediasystem;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;


public class NetworkKeyListener {
  private final JComponent component = new JButton();
  private final ExecutorService threadPool = Executors.newFixedThreadPool(3, new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r);

      thread.setDaemon(true);

      return thread;
    }
  });

  private final ServerSocket socket = new ServerSocket(1111);

  public NetworkKeyListener() throws IOException {
    Thread thread = new Thread() {
      @Override
      public void run() {
        try {
          for(;;) {
            @SuppressWarnings("resource")
            final Socket client = socket.accept();  // Blocks until there is a connection -- closing happens in spawned thread

            threadPool.execute(new Runnable() {
              @Override
              public void run() {
                try {
                  client.setSoTimeout(500);

                  try(PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                      LineNumberReader reader = new LineNumberReader(new InputStreamReader(client.getInputStream()))) {
                    for(;;) {
                      String line = reader.readLine();

                      if(line == null) {
                        break;
                      }

                      if(line.equals("quintessence")) {
                        writer.print("cookie");
                        writer.flush();
                      }
                      else if(line.equals("f882a23cc28ace6fd543abc94322344e")) {
                        writer.print("accept");
                        writer.flush();
                      }
                      else if(line.startsWith("EVENT ")) {
                        System.out.println(">>> " + line);

                        KeyStroke stroke = KeyStroke.getKeyStroke(line.substring(6));
                        KeyEvent event = new KeyEvent(component, stroke.getKeyEventType(), System.currentTimeMillis(), stroke.getModifiers(), stroke.getKeyCode(), stroke.getKeyChar());

                        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);
                        break;
                      }
                    }
                  }
                }
                catch (IOException e) {
                  e.printStackTrace();
                }
                finally {
                  try {
                    client.close();
                  }
                  catch (IOException e) {
                    // Not interested
                  }
                }
              }
            });
          }
        }
        catch(Exception e) {
          e.printStackTrace();
        }
      }
    };

    thread.setDaemon(true);
    thread.start();
  }
}
