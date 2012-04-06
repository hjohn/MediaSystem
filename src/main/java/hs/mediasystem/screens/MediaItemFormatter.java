package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.ThreadSafeDateFormat;

import java.text.DateFormat;
import java.util.Date;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

public class MediaItemFormatter {
  private static final ThreadSafeDateFormat DATE_FORMAT = new ThreadSafeDateFormat(DateFormat.getDateInstance(DateFormat.MEDIUM));

  public static StringBinding releaseTimeBinding(final MediaItem item) {
    return new StringBinding() {
      {
        bind(item.releaseDateProperty(), item.releaseYearProperty());
      }

      @Override
      protected String computeValue() {
        String releaseTime = item.getReleaseDate() == null ? null : DATE_FORMAT.format(item.getReleaseDate());

        if(releaseTime == null) {
          releaseTime = item.getReleaseYear() == null ? "" : "" + item.getReleaseYear();
        }

        return releaseTime;
      }
    };
  }

  public static StringBinding releaseYearBinding(final MediaItem item) {
    return new StringBinding() {
      {
        bind(item.releaseDateProperty(), item.releaseYearProperty());
      }

      @Override
      protected String computeValue() {
        String releaseTime = item.getReleaseDate() == null ? null : String.format("%tY", item.getReleaseDate());

        if(releaseTime == null) {
          releaseTime = item.getReleaseYear() == null ? "" : "" + item.getReleaseYear();
        }

        return releaseTime;
      }
    };
  }

  public static StringBinding releaseTimeBinding(final ObservableValue<MediaItem> item) {
    return new StringBinding() {
      final ObjectBinding<Date> selectReleaseDate = Bindings.select(item, "releaseDate");
      final ObjectBinding<Integer> selectReleaseYear = Bindings.select(item, "releaseYear");

      {
        bind(selectReleaseDate, selectReleaseYear);
      }

      @Override
      protected String computeValue() {
        String releaseTime = selectReleaseDate.get() == null ? null : DATE_FORMAT.format(selectReleaseDate.get());

        if(releaseTime == null) {
          releaseTime = selectReleaseYear.get() == null ? "" : "" + selectReleaseYear.get();
        }

        return releaseTime;
      }
    };
  }
}
