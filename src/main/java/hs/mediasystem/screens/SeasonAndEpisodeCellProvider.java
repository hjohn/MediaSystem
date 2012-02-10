package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;

public class SeasonAndEpisodeCellProvider implements CellProvider<MediaItem> {
  private final DuoLineCell cell = new DuoLineCell();

  @Override
  public Node configureCell(MediaItem item) {
    cell.titleProperty().bind(item.titleProperty());
    cell.subtitleProperty().set("");
    cell.extraInfoProperty().bind(Bindings.convert(item.episodeProperty()));
    cell.ratingProperty().bind(item.ratingProperty().divide(10));

    return cell;
  }
}