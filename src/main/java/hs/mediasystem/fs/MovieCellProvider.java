package hs.mediasystem.fs;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.MediaItemFormatter;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;

public class MovieCellProvider implements CellProvider<MediaItem> {
  private final DuoLineCell cell = new DuoLineCell();

  @Override
  public Node configureCell(MediaItem item) {
    cell.titleProperty().bind(item.titleProperty());
    cell.subtitleProperty().bind(item.subtitleProperty());

    if(item.getParent() != null) {
      if(item.getSubtitle() != null && !item.getSubtitle().isEmpty()) {
        cell.titleProperty().bind(item.subtitleProperty());
        cell.subtitleProperty().unbind();
        cell.subtitleProperty().set("");
      }
      else if(item.getEpisode() != null && item.getEpisode() > 1) {
        cell.titleProperty().bind(Bindings.concat(item.titleProperty(), " ", item.episodeProperty()));
        cell.subtitleProperty().bind(item.subtitleProperty());
      }
    }

    cell.extraInfoProperty().bind(MediaItemFormatter.releaseTimeBinding(item));
    cell.ratingProperty().bind(item.ratingProperty().divide(10));
    cell.groupProperty().set(item instanceof hs.mediasystem.framework.Group);

    return cell;
  }
}