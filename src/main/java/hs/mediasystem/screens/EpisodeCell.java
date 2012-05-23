package hs.mediasystem.screens;

import hs.mediasystem.framework.ConfigurableCell;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.media.Episode;
import hs.mediasystem.media.Media;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.WeakBinder;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;

public class EpisodeCell extends DuoLineCell implements ConfigurableCell<MediaNode> {
  private final WeakBinder binder = new WeakBinder();

  @Override
  public void configureCell(MediaNode mediaNode) {
    MediaItem item = mediaNode.getMediaItem();
    StringBinding episodeRange = MapBindings.selectString(mediaNode.mediaItemProperty(), "dataMap", Episode.class, "episodeRange");

    binder.unbindAll();

    binder.bind(titleProperty(), MapBindings.selectString(mediaNode.mediaItemProperty(), "dataMap", Media.class, "title"));
    binder.bind(ratingProperty(), MapBindings.selectDouble(mediaNode.mediaItemProperty(), "dataMap", Media.class, "rating").divide(10));
    binder.bind(extraInfoProperty(), Bindings.when(episodeRange.isNull()).then(new SimpleStringProperty("Special")).otherwise(episodeRange));
    binder.bind(viewedProperty(), item.viewedProperty());

    subtitleProperty().set("");
  }
}