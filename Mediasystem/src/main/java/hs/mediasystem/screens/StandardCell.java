package hs.mediasystem.screens;

import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.WeakBinder;

public class StandardCell extends DuoLineCell implements MediaNodeCell {
  private final WeakBinder binder = new WeakBinder();

  @Override
  public void attach(MediaNode mediaNode) {
    binder.bind(titleProperty(), MapBindings.selectString(mediaNode.media, "title"));
    binder.bind(subtitleProperty(), MapBindings.selectString(mediaNode.media, "subtitle"));

    collectionSizeProperty().set(mediaNode.getChildren().size());

    binder.bind(ratingProperty(), MapBindings.selectDouble(mediaNode.media, "rating").divide(10));
    binder.bind(viewedProperty(), MapBindings.selectBoolean(mediaNode.mediaData, "viewed"));
  }

  @Override
  public void detach() {
    binder.unbindAll();
  }
}