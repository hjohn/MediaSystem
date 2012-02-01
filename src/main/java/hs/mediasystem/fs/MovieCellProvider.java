package hs.mediasystem.fs;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.MediaItemFormatter;
import javafx.scene.Node;

public class MovieCellProvider implements CellProvider<MediaItem> {
  private final DuoLineCell cell = new DuoLineCell();

  @Override
  public Node configureCell(MediaItem item) {
    String title = item.getTitle();
    String subtitle = item.getSubtitle();

    if(item.getParent() != null) {
      if(item.getSubtitle() != null && !item.getSubtitle().isEmpty()) {
        title = item.getSubtitle();
        subtitle = "";
      }
      else if(item.getEpisode() != null && item.getEpisode() > 1) {
        title += " " + item.getEpisode();
      }
    }

    cell.titleProperty().set(title);
    cell.subtitleProperty().set(subtitle);
    cell.extraInfoProperty().set(MediaItemFormatter.formatReleaseTime(item));
    cell.ratingProperty().set(item.getRating() == null ? 0.0 : item.getRating() / 10);
    cell.groupProperty().set(item instanceof hs.mediasystem.framework.Group);

    return cell;
  }
}