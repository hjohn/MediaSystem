package hs.mediasystem.ext.media.serie;

import hs.mediasystem.screens.DuoLineCell;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.WeakBinder;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;

public class EpisodeCell extends DuoLineCell implements MediaNodeCell {
  private final WeakBinder binder = new WeakBinder();

  @Override
  public void attach(MediaNode mediaNode) {
    StringBinding episodeRange = MapBindings.selectString(mediaNode.media, "episodeRange");

    binder.bind(titleProperty(), MapBindings.selectString(mediaNode.media, "title"));
    binder.bind(ratingProperty(), MapBindings.selectDouble(mediaNode.media, "rating").divide(10));
    binder.bind(extraInfoProperty(), Bindings.when(episodeRange.isNull()).then(new SimpleStringProperty("Special")).otherwise(episodeRange));
    binder.bind(viewedProperty(), MapBindings.selectBoolean(mediaNode.mediaData, "viewed"));

    subtitleProperty().set("");
  }

  @Override
  public void detach() {
    binder.unbindAll();
  }
}