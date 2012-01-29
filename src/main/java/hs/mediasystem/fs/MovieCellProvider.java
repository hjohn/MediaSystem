package hs.mediasystem.fs;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.MediaItemFormatter;
import javafx.scene.Node;

public class MovieCellProvider implements CellProvider<MediaItem> {
  private final DuoLineCell cell = new DuoLineCell();

  @Override
  public Node configureCell(MediaItem item) {
    cell.titleProperty().set(item.getTitle());
    cell.subtitleProperty().set(item.getSubtitle());
    cell.extraInfoProperty().set(MediaItemFormatter.formatReleaseTime(item));
    cell.ratingProperty().set(item.getRating() == null ? 0.0 : item.getRating() / 10);
    cell.groupProperty().set(item instanceof hs.mediasystem.framework.Group);

    return cell;
  }
}