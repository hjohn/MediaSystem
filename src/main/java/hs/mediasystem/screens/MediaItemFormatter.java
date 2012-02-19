package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;

import java.text.DateFormat;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

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

  public static synchronized StringBinding releaseTimeBinding(final ObjectBinding<MediaItem> item) {
    return new StringBinding() {
      {
        item.addListener(new ChangeListener<MediaItem>() {
          @Override
          public void changed(ObservableValue<? extends MediaItem> observable, MediaItem oldValue, MediaItem value) {
            if(oldValue != null) {
              unbind(oldValue.releaseDateProperty(), oldValue.releaseYearProperty());
            }
            if(value != null) {
              bind(value.releaseDateProperty(), value.releaseYearProperty());
            }
          }
        });
      }

      @Override
      protected String computeValue() {
        MediaItem currentItem = item.get();

        return currentItem == null ? "" : formatReleaseTime(currentItem);
      }
    };
  }
}
