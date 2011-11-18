package hs.mediasystem;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Renderer;
import hs.mediasystem.fs.MediaItemRenderer;
import hs.mediasystem.fs.NamedItem;
import hs.mediasystem.screens.movie.ItemUpdate;
import hs.models.events.ListenerList;
import hs.models.events.Notifier;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ListCell;

public class Nederland24MediaTree implements MediaTree {
  private final Notifier<ItemUpdate> itemUpdateNotifier = new Notifier<>();

  @Override
  public void refresh() {
  }

  @Override
  public Style getStyle() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public List<? extends MediaItem> children() {
    return new ArrayList<Nederland24Item>() {{
      add(new Nederland24Item(Nederland24MediaTree.this, "Journaal 24", "http://livestreams.omroep.nl/nos/journaal24-bb"));
    }};
  }

  @Override
  public MediaTree parent() {
    return null;
  }

  @Override
  public Renderer<MediaItem> getRenderer() {
    return new MediaItemRenderer();
  }

  @Override
  public ListenerList<ItemUpdate> onItemUpdate() {
    return itemUpdateNotifier.getListenerList();
  }

  @Override
  public void triggerItemUpdate(NamedItem namedItem) {
  }

  @Override
  public ListCell<MediaItem> createListCell() {
    throw new UnsupportedOperationException("Method not implemented");
  }
}
