package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.WeakBinder;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;

public class SeasonAndEpisodeCellProvider implements CellProvider<MediaNode> {
  private final DuoLineCell cell = new DuoLineCell();
  private final WeakBinder binder = new WeakBinder();

  @Override
  public Node configureCell(MediaNode mediaNode) {
    MediaItem item = mediaNode.getMediaItem();

    binder.unbindAll();

    binder.bind(cell.titleProperty(), item.titleProperty());
    binder.bind(cell.ratingProperty(), item.ratingProperty().divide(10));
    binder.bind(cell.extraInfoProperty(), Bindings.when(item.episodeProperty().isNull()).then(new SimpleStringProperty("Special")).otherwise(Bindings.convert(item.episodeProperty())));
    binder.bind(cell.viewedProperty(), item.viewedProperty());

//    cell.titleProperty().bind(item.titleProperty());
    cell.subtitleProperty().set("");
//    cell.extraInfoProperty().bind(Bindings.when(item.episodeProperty().isNull()).then(new SimpleStringProperty("Special")).otherwise(Bindings.convert(item.episodeProperty())));
//    cell.ratingProperty().bind(item.ratingProperty().divide(10));
//    cell.viewedProperty().bind(item.viewedProperty());

    return cell;
  }
}