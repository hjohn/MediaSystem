package hs.mediasystem.screens;

import java.text.DateFormat;

import hs.mediasystem.framework.MediaItem;

public class MediaItemFormatter {
  private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

  public static synchronized String formatReleaseTime(MediaItem item) {
    String releaseTime = item.getReleaseDate() == null ? null : (dateFormat.format(item.getReleaseDate()) + " ");
    if(releaseTime == null) {
      releaseTime = item.getReleaseYear() == null ? "" : ("" + item.getReleaseYear() + " ");
    }

    return releaseTime;
  }
}
