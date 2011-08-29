package hs.mediasystem;

import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.screens.movie.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EpisodeGroup extends Episode {
  private final List<Episode> episodes = new ArrayList<Episode>();
  
  public EpisodeGroup(Element element, ItemProvider itemProvider) {
    super(element, itemProvider);
  }

  public void add(Episode episode) {
    episodes.add(episode);
    episode.episodeGroup = this;
  }

  public int size() {
    return episodes.size();
  }

  public Collection<? extends Episode> episodes() {
    return Collections.unmodifiableCollection(episodes);
  }
}
