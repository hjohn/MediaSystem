package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import javafx.scene.Node;

public class MovieCellProvider implements CellProvider<MediaNode> {
  private final DuoLineCell cell = new DuoLineCell();

  @Override
  public Node configureCell(MediaNode mediaNode) {
    MediaItem item = mediaNode.getMediaItem();

    cell.titleProperty().bind(item.titleProperty());
    cell.subtitleProperty().bind(item.subtitleProperty());

    cell.extraInfoProperty().bind(MediaItemFormatter.releaseYearBinding(item));
    cell.ratingProperty().bind(item.ratingProperty().divide(10));
    cell.groupProperty().set(!item.isLeaf());

    cell.viewedProperty().bind(item.viewedProperty());

    return cell;
  }
}