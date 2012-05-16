package hs.mediasystem.screens;

import hs.mediasystem.util.ThreadSafeDateFormat;

import java.text.DateFormat;
import java.util.Date;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

public class MediaItemFormatter {
  private static final ThreadSafeDateFormat DATE_FORMAT = new ThreadSafeDateFormat(DateFormat.getDateInstance(DateFormat.MEDIUM));

  public static StringBinding releaseTimeBinding(final MediaNode node) {
    return new StringBinding() {
      {
        bind(node.releaseDateProperty(), node.releaseYearProperty());
      }

      @Override
      protected String computeValue() {
        String releaseTime = node.getReleaseDate() == null ? null : DATE_FORMAT.format(node.getReleaseDate());

        if(releaseTime == null) {
          releaseTime = node.getReleaseYear() == null ? "" : "" + node.getReleaseYear();
        }

        return releaseTime;
      }
    };
  }

  public static StringBinding releaseYearBinding(final MediaNode node) {
    return new StringBinding() {
      {
        bind(node.releaseDateProperty(), node.releaseYearProperty());
      }

      @Override
      protected String computeValue() {
        String releaseTime = node.getReleaseDate() == null ? null : String.format("%tY", node.getReleaseDate());

        if(releaseTime == null) {
          releaseTime = node.getReleaseYear() == null ? "" : "" + node.getReleaseYear();
        }

        return releaseTime;
      }
    };
  }

  public static StringBinding releaseTimeBinding(final ObservableValue<MediaNode> item) {
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
