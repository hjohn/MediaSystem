package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class MovieCellProvider implements CellProvider<MediaItem> {
  private final DuoLineCell cell = new DuoLineCell();

  @Override
  public Node configureCell(TreeItem<MediaItem> treeItem) {
    MediaItem item = treeItem.getValue();
    cell.titleProperty().bind(item.titleProperty());
    cell.subtitleProperty().bind(item.subtitleProperty());

    if(treeItem.getParent() != null && treeItem.getParent().getParent() != null) {
      if(item.getSubtitle() != null && !item.getSubtitle().isEmpty()) {
        cell.titleProperty().bind(item.subtitleProperty());
        cell.subtitleProperty().unbind();
        cell.subtitleProperty().set("");
      }
      else if(item.getEpisode() != null && item.getEpisode() > 1) {
        cell.titleProperty().bind(Bindings.concat(item.titleProperty(), " ", item.episodeProperty()));
      }
    }

    cell.extraInfoProperty().bind(MediaItemFormatter.releaseTimeBinding(item));
    cell.ratingProperty().bind(item.ratingProperty().divide(10));
    cell.groupProperty().set(!item.isLeaf());

    return cell;
  }
}