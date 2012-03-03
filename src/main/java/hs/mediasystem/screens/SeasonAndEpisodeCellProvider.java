package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;

public class SeasonAndEpisodeCellProvider implements CellProvider<MediaNode> {
  private final DuoLineCell cell = new DuoLineCell();

  @Override
  public Node configureCell(MediaNode mediaNode) {
    MediaItem item = mediaNode.getMediaItem();

    cell.titleProperty().bind(item.titleProperty());
    cell.subtitleProperty().set("");
    cell.extraInfoProperty().bind(Bindings.when(item.episodeProperty().isNull()).then(new SimpleStringProperty("Special")).otherwise(Bindings.convert(item.episodeProperty())));
    cell.ratingProperty().bind(item.ratingProperty().divide(10));

    return cell;
  }
}