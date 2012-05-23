package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.media.Media;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.WeakBinder;
import javafx.scene.Node;

public class StandardCellProvider implements CellProvider<MediaNode> {
  private final DuoLineCell cell = new DuoLineCell();
  private final WeakBinder binder = new WeakBinder();

  @Override
  public Node configureCell(MediaNode mediaNode) {
    MediaItem item = mediaNode.getMediaItem();

    binder.unbindAll();

    binder.bind(cell.titleProperty(), mediaNode.titleProperty());
    binder.bind(cell.subtitleProperty(), MapBindings.selectString(mediaNode.mediaItemProperty(), "dataMap", Media.class, "subtitle"));
    binder.bind(cell.extraInfoProperty(), MediaItemFormatter.releaseYearBinding(mediaNode));

    cell.collectionSizeProperty().set(mediaNode.getChildren().size());

    if(item != null) {
      binder.bind(cell.ratingProperty(), MapBindings.selectDouble(mediaNode.mediaItemProperty(), "dataMap", Media.class, "rating").divide(10));
      binder.bind(cell.viewedProperty(), item.viewedProperty());
    }
    else {
      cell.viewedProperty().set(false);
    }

    return cell;
  }
}