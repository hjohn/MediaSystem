package hs.mediasystem.fs;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import javafx.scene.Node;

public class SeasonAndEpisodeCellProvider implements CellProvider<MediaItem> {
  private final DuoLineCell cell = new DuoLineCell();

  @Override
  public Node configureCell(MediaItem item) {
    cell.titleProperty().set(item.getTitle());
    cell.subtitleProperty().set("");
    cell.extraInfoProperty().set("" + item.getEpisode());
    cell.ratingProperty().set(item.getRating() == null ? 0.0 : item.getRating() / 10);

    return cell;
  }
}