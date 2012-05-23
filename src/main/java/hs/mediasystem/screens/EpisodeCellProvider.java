package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.media.Episode;
import hs.mediasystem.media.Media;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.WeakBinder;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;

public class EpisodeCellProvider implements CellProvider<MediaNode> {
  private final DuoLineCell cell = new DuoLineCell();
  private final WeakBinder binder = new WeakBinder();

  @Override
  public Node configureCell(MediaNode mediaNode) {
    MediaItem item = mediaNode.getMediaItem();
    StringBinding episodeRange = MapBindings.selectString(mediaNode.mediaItemProperty(), "dataMap", Episode.class, "episodeRange");

    binder.unbindAll();

    binder.bind(cell.titleProperty(), MapBindings.selectString(mediaNode.mediaItemProperty(), "dataMap", Media.class, "title"));
    binder.bind(cell.ratingProperty(), MapBindings.selectDouble(mediaNode.mediaItemProperty(), "dataMap", Media.class, "rating").divide(10));
    binder.bind(cell.extraInfoProperty(), Bindings.when(episodeRange.isNull()).then(new SimpleStringProperty("Special")).otherwise(episodeRange));
    binder.bind(cell.viewedProperty(), item.viewedProperty());

    cell.subtitleProperty().set("");

    return cell;
  }
}