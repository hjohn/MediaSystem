package hs.mediasystem.screens;

import hs.mediasystem.media.Media;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ThreadSafeDateFormat;

import java.text.DateFormat;
import java.util.Date;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

public class MediaItemFormatter {
  private static final ThreadSafeDateFormat DATE_FORMAT = new ThreadSafeDateFormat(DateFormat.getDateInstance(DateFormat.MEDIUM));

  public static StringBinding releaseYearBinding(final MediaNode node) {
    return new StringBinding() {
      final ObjectBinding<Date> selectReleaseDate = MapBindings.select(node.dataMapProperty(), Media.class, "releaseDate");
      final ObjectBinding<Integer> selectReleaseYear = MapBindings.select(node.dataMapProperty(), Media.class, "releaseYear");

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

  public static StringBinding releaseTimeBinding(final ObservableValue<MediaNode> node) {
    return new StringBinding() {
      final ObjectBinding<Date> selectReleaseDate = MapBindings.select(node, "dataMap", Media.class, "releaseDate");
      final ObjectBinding<Integer> selectReleaseYear = MapBindings.select(node, "dataMap", Media.class, "releaseYear");

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
