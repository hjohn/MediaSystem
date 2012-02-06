package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;

import java.text.DateFormat;

import javafx.beans.binding.StringBinding;

public class MediaItemFormatter {
  private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

  public static synchronized String formatReleaseTime(MediaItem item) {
    String releaseTime = item.getReleaseDate() == null ? null : dateFormat.format(item.getReleaseDate());
    if(releaseTime == null) {
      releaseTime = item.getReleaseYear() == null ? "" : "" + item.getReleaseYear();
    }

    return releaseTime;
  }

  public static synchronized StringBinding releaseTimeBinding(final MediaItem item) {
    return new StringBinding() {
      {
        this.bind(item.releaseDateProperty(), item.releaseYearProperty());
      }

      @Override
      protected String computeValue() {
        return formatReleaseTime(item);
      }
    };
  }
}
