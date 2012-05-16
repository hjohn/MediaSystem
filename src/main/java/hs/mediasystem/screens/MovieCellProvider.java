package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.WeakBinder;
import javafx.scene.Node;

public class MovieCellProvider implements CellProvider<MediaNode> {
  private final DuoLineCell cell = new DuoLineCell();
  private final WeakBinder binder = new WeakBinder();

  @Override
  public Node configureCell(MediaNode mediaNode) {
    MediaItem item = mediaNode.getMediaItem();

    binder.unbindAll();

    binder.bind(cell.titleProperty(), mediaNode.titleProperty());
    binder.bind(cell.subtitleProperty(), mediaNode.subtitleProperty());
    binder.bind(cell.extraInfoProperty(), MediaItemFormatter.releaseYearBinding(mediaNode));

    cell.collectionSizeProperty().set(mediaNode.getChildren().size());

    if(item != null) {
      binder.bind(cell.ratingProperty(), item.ratingProperty().divide(10));
      binder.bind(cell.viewedProperty(), item.viewedProperty());
    }
    else {
      cell.viewedProperty().set(false);
    }

    return cell;
  }
}