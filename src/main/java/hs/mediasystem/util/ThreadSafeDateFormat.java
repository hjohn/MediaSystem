package hs.mediasystem.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class ThreadSafeDateFormat {
  private final DateFormat dateFormat;

  public ThreadSafeDateFormat(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }

  public synchronized String format(Date date) {
    return dateFormat.format(date);
  }

  public synchronized Date parse(String date) throws ParseException {
    return dateFormat.parse(date);
  }
}