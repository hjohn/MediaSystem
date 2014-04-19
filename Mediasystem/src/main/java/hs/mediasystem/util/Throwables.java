package hs.mediasystem.util;

public class Throwables {
  public static String formatAsOneLine(Throwable t) {
    StringBuilder sb = new StringBuilder();
    Throwable current = t;

    for(;;) {
      sb.append(current.getClass().getName());

      if(current.getMessage() != null) {
        sb.append(" [");
        sb.append(current.getMessage());
        sb.append("]");
      }

      sb.append(" @ ");
      sb.append(current.getStackTrace()[0]);

      current = current.getCause();

      if(current == null) {
        break;
      }

      sb.append(" --> ");
    }

    return sb.toString();
  }
}
