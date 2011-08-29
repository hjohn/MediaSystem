package hs.mediasystem;

import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.screens.movie.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Serie {
  private final List<Episode> episodes = new ArrayList<Episode>();
  
  public Episode addEpisode(Element element, ItemProvider itemProvider) {
    Episode episode = new Episode(element, itemProvider);
    
    episodes.add(episode);
    
    return episode;
  }

  public Collection<Episode> episodes() {
    return Collections.unmodifiableList(episodes);
  }
  
  public Collection<? extends Episode> episodes(Grouper grouper) {
    Map<Object, EpisodeGroup> groupedEpisodes = new HashMap<Object, EpisodeGroup>();
    
    for(Episode episode : episodes) {
      Object group = grouper.getGroup(episode);
      
      EpisodeGroup episodeGroup = groupedEpisodes.get(group);
      
      if(episodeGroup == null) {
        episodeGroup = new EpisodeGroup(new Element(null, episode.getTitle(), "", null, null, null), null);
        groupedEpisodes.put(group, episodeGroup);
      }
      
      episodeGroup.add(episode);
    }
    
    List<Episode> episodes = new ArrayList<Episode>();
    
    for(EpisodeGroup episodeGroup : groupedEpisodes.values()) {
      if(episodeGroup.size() > 1) {
        episodes.add(episodeGroup);
      }
      else {
        Episode episode = episodeGroup.episodes().iterator().next();
        episode.episodeGroup = null;
        episodes.add(episode);
      }
    }
    
    return episodes;
  }
}
