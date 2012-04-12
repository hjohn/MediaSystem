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

    binder.bind(cell.titleProperty(), item.titleProperty());
    binder.bind(cell.subtitleProperty(), item.subtitleProperty());

    binder.bind(cell.extraInfoProperty(), MediaItemFormatter.releaseYearBinding(item));
    binder.bind(cell.ratingProperty(), item.ratingProperty().divide(10));

    cell.collectionSizeProperty().set(item.children().size());

    binder.bind(cell.viewedProperty(), (item.viewedProperty()));

//    cell.titleProperty().bind(item.titleProperty());
//    cell.subtitleProperty().bind(item.subtitleProperty());
//
//    cell.extraInfoProperty().bind(MediaItemFormatter.releaseYearBinding(item));
//    cell.ratingProperty().bind(item.ratingProperty().divide(10));
//    cell.collectionSizeProperty().set(item.children().size());
//
//    cell.viewedProperty().bind(item.viewedProperty());

    return cell;
  }
}