package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaNodeCell;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.media.Media;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.WeakBinder;

public class StandardCell extends DuoLineCell implements MediaNodeCell {
  private final WeakBinder binder = new WeakBinder();

  @Override
  public void configureCell(MediaNode mediaNode) {
    MediaItem item = mediaNode.getMediaItem();

    binder.unbindAll();

    binder.bind(titleProperty(), mediaNode.titleProperty());
    binder.bind(subtitleProperty(), MapBindings.selectString(mediaNode.mediaItemProperty(), "dataMap", Media.class, "subtitle"));
    binder.bind(extraInfoProperty(), MediaItemFormatter.releaseYearBinding(mediaNode));

    collectionSizeProperty().set(mediaNode.getChildren().size());

    if(item != null) {
      binder.bind(ratingProperty(), MapBindings.selectDouble(mediaNode.mediaItemProperty(), "dataMap", Media.class, "rating").divide(10));
      binder.bind(viewedProperty(), item.viewedProperty());
    }
    else {
      viewedProperty().set(false);
    }
  }
}