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

    // FIXME see how this can be solved without resorting to using TreeItem
//    if(mediaNode.getParent() != null && mediaNode.getParent().getParent() != null) {
//      if(item.getSubtitle() != null && !item.getSubtitle().isEmpty()) {
//        cell.titleProperty().bind(item.subtitleProperty());
//        cell.subtitleProperty().unbind();
//        cell.subtitleProperty().set("");
//      }
//      else if(item.getEpisode() != null && item.getEpisode() > 1) {
//        cell.titleProperty().bind(Bindings.concat(item.titleProperty(), " ", item.episodeProperty()));
//      }
//    }

    cell.extraInfoProperty().bind(MediaItemFormatter.releaseTimeBinding(item));
    cell.ratingProperty().bind(item.ratingProperty().divide(10));
    cell.groupProperty().set(!item.isLeaf());

    return cell;
  }
}