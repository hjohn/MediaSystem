package hs.mediasystem.fs;

import hs.mediasystem.ImageHandle;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.nio.file.Path;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

public abstract class NamedItem implements MediaItem {
  private final MediaTree mediaTree;
  private final LocalInfo localInfo;

  protected MediaItem parent;

  private Item item = new Item();

  public NamedItem(MediaTree mediaTree, LocalInfo localInfo) {
    this.mediaTree = mediaTree;
    this.localInfo = localInfo;

    item.setType(localInfo.getType().name());
    item.setTitle(localInfo.getTitle());
    item.setSubtitle(localInfo.getSubtitle());
    item.setReleaseYear(localInfo.getReleaseYear());
    item.setImdbId(localInfo.getCode());
    item.setSeason(localInfo.getSeason());
    item.setEpisode(localInfo.getEpisode());
  }

  Item getItem() {
    return item;
  }

  @Override
  public final String getTitle() {
    return localInfo.getTitle();
  }

  public MediaItem getParent() {
    return parent;
  }

  public Path getPath() {
    return localInfo.getPath();
  }

  @Override
  public String getUri() {
    return localInfo.getPath().toString();
  }

  @Override
  public String getSubtitle() {
    return localInfo.getSubtitle() == null ? "" : localInfo.getSubtitle();
  }

  @Override
  public Integer getReleaseYear() {
    return localInfo.getReleaseYear();
//    if(localInfo.getReleaseDate() == null) {
//      return null;
//    }
//
//    GregorianCalendar gc = new GregorianCalendar(2000, 0, 1);
//    gc.setTime(localInfo.getReleaseDate());
//    return "" + gc.get(Calendar.YEAR);
  }

  @Override
  public Integer getSeason() {
    return localInfo.getSeason();
  }

  @Override
  public Integer getEpisode() {
    return localInfo.getEpisode();
  }

  @Override
  public ImageHandle getBackground() {
    return item.getBackground() == null ? null : new ImageHandle(item.getBackground(), createKey("background"));
  }

  @Override
  public ImageHandle getBanner() {
    return item.getBanner() == null ? null : new ImageHandle(item.getBanner(), createKey("banner"));
  }

  @Override
  public ImageHandle getPoster() {
    return item.getPoster() == null ? null : new ImageHandle(item.getPoster(), createKey("poster"));
  }

  private String createKey(String suffix) {
    return getTitle() + "-" + getSeason() + "x" + getEpisode() + "-" + getSubtitle() + "-" + suffix;
  }

  @Override
  public String getPlot() {
    return item.getPlot();
  }

  public int getRuntime() {
    return item.getRuntime();
  }

  public Float getRating() {
    return item.getRating();
  }

  public String getProvider() {
    return item.getProvider();
  }

  public String getProviderId() {
    return item.getProviderId();
  }

  private ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.BASIC);

  public State getState() {
    return state.get();
  }

  @Override
  public ReadOnlyObjectProperty<State> stateProperty() {
    return state.getReadOnlyProperty();
  }

  @Override
  public void loadData(ItemEnricher itemEnricher) {
    synchronized(state) {
      if(getState() != State.BASIC) {
        return;
      }

      state.set(State.ENRICHING);
    }

    synchronized(NamedItem.class) {
      if(item.getId() == 0 && mediaTree != null) {
        item.setId(-1);
        mediaTree.enrichItem(itemEnricher, this);
      }
    }

    state.set(State.ENRICHED);
  }
}
