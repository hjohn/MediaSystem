package hs.mediasystem;

import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.CellProvider;
import hs.mediasystem.fs.NamedItem;

import java.util.ArrayList;
import java.util.List;

public class Nederland24MediaTree implements MediaTree {

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
  public void enrichItem(ItemEnricher itemEnricher, NamedItem namedItem) {
  }

  @Override
  public CellProvider<MediaItem> createListCell() {
    throw new UnsupportedOperationException("Method not implemented");
  }
}
