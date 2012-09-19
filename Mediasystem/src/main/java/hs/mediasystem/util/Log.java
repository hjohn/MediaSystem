package hs.mediasystem.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log provides a very simple and unobtrusive way of generating logs with timestamps and
 * location of the log statement.  It redirects System.out and System.err and decorates
 * any data send there with timestamp and location, for example:<p>
 *
 * <code>System.out.println("Hello World");</code><p>
 * becomes:<p>
 * <code>[FINE 10-07-2007 10:54:15.274] Hello World&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-- com.example.logging.Hello.main(Hello.java:7)</code><p>
 * Lines can be prefixed with the standard Log Level prefixes found in {@link Level}, for example:<p>
 * <code>System.out.println("[WARNING] Hello World");</code><p>
 */
public class Log {
  private static PrintStream outputStream;
  private static PrintStream errorStream;

  @SuppressWarnings("resource")
  public static void initialize(Level level, LinePrinter linePrinter) {
    LinePrinter finalLinePrinter = linePrinter;

    if(finalLinePrinter == null) {
      finalLinePrinter = new LinePrinter() {
        private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS", Locale.ROOT);

        @Override
        public void print(PrintStream printStream, Level level, String text, String method) {
          String end = "";
          int len = text.length();

          if(!Character.isWhitespace(text.charAt(0))) {
            end = "                ".substring(0, 15 - len % 16) + method;
          }

          printStream.print("[" + level.toString().substring(0, 4) + " " + dateFormat.format(new Date()) + "] " + text + end + "\r\n");
        }
      };
    }

    outputStream = System.out;
    errorStream = System.err;

    System.setOut(new PrintStream(new LogStream(outputStream, Level.FINE, level, finalLinePrinter)));
    System.setErr(new PrintStream(new LogStream(errorStream, Level.SEVERE, level, finalLinePrinter)));
  }

  public static void initialize() {
    initialize(Level.FINEST, null);
  }

  public static void initialize(LinePrinter linePrinter) {
    initialize(Level.FINEST, linePrinter);
  }


  private static class LogStream extends OutputStream {
    private static final Pattern LOGLINE = Pattern.compile("(\\[([A-Z]+)\\] ?)?(.*)");
    private static final Charset UTF8 = Charset.forName("utf8");

    private final PrintStream printStream;
    private final Level defaultLevel;
    private final Level minimumLevel;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final LinePrinter linePrinter;

    public LogStream(PrintStream printStream, Level defaultLevel, Level minimumLevel, LinePrinter linePrinter) {
      this.printStream = printStream;
      this.defaultLevel = defaultLevel;
      this.minimumLevel = minimumLevel;
      this.linePrinter = linePrinter;
    }

    @Override
    public synchronized void write(int b) throws IOException {
      try {
        if(b == 10 || b == 13) {
          if(baos.size() > 0) {
            writeLine(new String(baos.toByteArray(), UTF8));
            baos.reset();
          }
        }
        else {
          baos.write(b);
        }
      }
      catch(Exception e) {
        e.printStackTrace(outputStream);
      }
    }

    private void writeLine(String s) {
      // SEVERE, WARNING, INFO, CONFIG, (FINE), FINER, FINEST
      Matcher matcher = LOGLINE.matcher(s);

      matcher.matches();

      Level level = defaultLevel;

      if(matcher.group(2) != null) {
        String levelText = matcher.group(2);

        if(levelText.equals("WARN")) {
          levelText = "WARNING";
        }
        level = Level.parse(levelText);
      }

      if(level.intValue() >= minimumLevel.intValue()) {
        String text = matcher.group(3);

        linePrinter.print(printStream, level, text, method());
      }
    }

    private static String method() {
      StackTraceElement[] elements = new Throwable().getStackTrace();
      boolean foundLogSystem = false;

      for(int i = 0; i < elements.length; i++) {
        String frame = elements[i].toString();

        if(frame.startsWith("java.io.PrintStream.println(") || frame.startsWith("java.io.PrintStream.printf(") || frame.startsWith("java.util.logging.Logger.")) {
          foundLogSystem = true;
        }
        else if(foundLogSystem) {
          return " -- " + frame;
        }
      }

      return "";
    }
  }

  public interface LinePrinter {
    void print(PrintStream printStream, Level level, String text, String method);
  }
}
