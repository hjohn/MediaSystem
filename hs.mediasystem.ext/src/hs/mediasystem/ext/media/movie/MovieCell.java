package hs.mediasystem.ext.media.movie;

import hs.mediasystem.screens.DuoLineCell;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.WeakBinder;

public class MovieCell extends DuoLineCell implements MediaNodeCell {
  private final WeakBinder binder = new WeakBinder();

  @Override
  public void attach(MediaNode mediaNode) {
    binder.unbindAll();

    binder.bind(titleProperty(), mediaNode.title);
    binder.bind(subtitleProperty(), mediaNode.subtitle);
    binder.bind(extraInfoProperty(), MediaItemFormatter.releaseYearBinding(mediaNode));

    collectionSizeProperty().set(mediaNode.getChildren().size());

    binder.bind(ratingProperty(), MapBindings.selectDouble(mediaNode.media, "rating").divide(10));
    binder.bind(viewedProperty(), MapBindings.selectBoolean(mediaNode.mediaData, "viewed"));
  }

  @Override
  public void detach() {
    binder.unbindAll();
  }
}