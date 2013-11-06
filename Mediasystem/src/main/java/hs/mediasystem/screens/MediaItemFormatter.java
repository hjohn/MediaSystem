package hs.mediasystem.screens;

import hs.mediasystem.framework.Media;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ThreadSafeDateFormat;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

public class MediaItemFormatter {
  private static final ThreadSafeDateFormat DATE_FORMAT = new ThreadSafeDateFormat(DateFormat.getDateInstance(DateFormat.MEDIUM));
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

  public static StringBinding releaseYearBinding(final MediaNode node) {
    return new StringBinding() {
      final ObjectBinding<LocalDate> selectReleaseDate = MapBindings.select(node.media, "releaseDate");
      final ObjectBinding<Integer> selectReleaseYear = MapBindings.select(node.properties, "releaseYear");

      {
        bind(selectReleaseDate, selectReleaseYear);
      }

      @Override
      protected String computeValue() {
        String releaseTime = selectReleaseDate.get() == null ? null : String.format("%tY", selectReleaseDate.get());

        if(releaseTime == null) {
          releaseTime = selectReleaseYear.get() == null ? "" : "" + selectReleaseYear.get();
        }

        return releaseTime;
      }
    };
  }

  public static StringBinding releaseTimeBinding(final ObservableValue<Media<?>> media) {
    return new StringBinding() {
      final ObjectBinding<LocalDate> selectReleaseDate = MapBindings.select(media, "releaseDate");

      {
        bind(selectReleaseDate);
      }

      @Override
      protected String computeValue() {
        return selectReleaseDate.get() == null ? "" : DATE_TIME_FORMATTER.format(selectReleaseDate.get());
      }
    };
  }

  public static StringBinding formattedDate(final ObservableValue<Date> date) {
    return new StringBinding() {
      {
        bind(date);
      }

      @Override
      protected String computeValue() {
        String releaseTime = date.getValue() == null ? null : DATE_FORMAT.format(date.getValue());

        return releaseTime;
      }
    };
  }
}
