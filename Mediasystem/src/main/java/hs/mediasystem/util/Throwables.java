package hs.mediasystem.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Throwables {

  public static String formatAsOneLine(Throwable throwable) {
    StringBuilder builder = new StringBuilder();
    Throwable current = throwable;

    for(;;) {
      builder.append(current.getClass().getName());

      if(current.getMessage() != null) {
        builder.append(" [");
        builder.append(current.getMessage());
        builder.append("]");
      }

      builder.append(" @ ");
      builder.append(current.getStackTrace()[0]);

      current = current.getCause();

      if(current == null) {
        break;
      }

      builder.append(" --> ");
    }

    return builder.toString();
  }

  public static String toString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();

    throwable.printStackTrace(new PrintWriter(stringWriter));

    return stringWriter.toString();
  }
}
