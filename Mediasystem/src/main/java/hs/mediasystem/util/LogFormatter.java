package hs.mediasystem.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
  private static final long STARTUP_MILLIS = System.currentTimeMillis();
  private static final int METHOD_COLUMN = 160;
  private static final int METHOD_TAB_STOP = 24;
  private static final String PAD = String.format("%" + METHOD_COLUMN + "s", "");
  private static final String PIPE = "\u2502";
  private static final Map<Level, String> INDICATOR_BY_LEVEL = new HashMap<Level, String>() {{
    put(Level.SEVERE, "!");
    put(Level.WARNING, "?");
    put(Level.INFO, "*");
    put(Level.CONFIG, "+");
    put(Level.FINE, " ");
    put(Level.FINER, "-");
    put(Level.FINEST, "=");
  }};

  @Override
  public String format(LogRecord record) {
    Throwable throwable = record.getThrown();
    StringBuilder builder = new StringBuilder();
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();

    builder.append(String.format(
      "%1s%9.3f" + PIPE + "%3d" + PIPE + "%02x" + PIPE,
      INDICATOR_BY_LEVEL.get(record.getLevel()),
      ((double)(System.currentTimeMillis() - STARTUP_MILLIS)) / 1000,
      usedMemory / 1024 / 1024,
      Thread.currentThread().getId()
    ));

    builder.append(getFixedLengthLoggerName(record.getLoggerName())).append(PIPE);
    builder.append(record.getMessage());

    if(builder.length() < METHOD_COLUMN) {
      builder.append(PAD.substring(0, METHOD_COLUMN - builder.length()));
    }
    else {
      builder.append(PAD.substring(0, METHOD_TAB_STOP - builder.length() % METHOD_TAB_STOP));
    }

    StackTraceElement element = getCallerStackFrame(record.getSourceClassName());

    builder.append("-- ").append(element);
    builder.append("\n");

    if(throwable != null) {
      builder.append(Throwables.toString(throwable));
      builder.append("\n");
    }

    return builder.toString();
  }

  private String getTruncatedLoggerName(String loggerName) {
    String truncatedLoggerName = loggerName;

    while(truncatedLoggerName.length() > 40) {
      int periodIndex = truncatedLoggerName.indexOf(".");

      if(periodIndex == -1) {
        break;
      }

      int colonIndex = truncatedLoggerName.indexOf(":");

      truncatedLoggerName =
          (colonIndex == -1 ? "" : truncatedLoggerName.substring(0, colonIndex)) +
          truncatedLoggerName.substring(colonIndex + 1, colonIndex + 2) + ":" +
          truncatedLoggerName.substring(periodIndex + 1);
    }

    return truncatedLoggerName;
  }

  private String getFixedLengthLoggerName(String loggerName) {
    String truncatedLoggerName = getTruncatedLoggerName(loggerName);

    return truncatedLoggerName.length() < 40 ? truncatedLoggerName + PAD.substring(0, 40 - truncatedLoggerName.length()) : truncatedLoggerName;
  }

  private StackTraceElement getCallerStackFrame(String callerName) {
    final StackTraceElement stack[] = new Throwable().getStackTrace();

    for(int i = 0; i < stack.length; i++) {
      StackTraceElement frame = stack[i];

      if(callerName.equals(frame.getClassName())) {
        return frame;
      }
    }

    return null;
  }
}