package hs.mediasystem;

import hs.mediasystem.screens.movie.Element;

import java.util.ArrayList;
import java.util.List;

public class Serie {
  private final List<Episode> episodes = new ArrayList<Episode>();
  
  public Episode addEpisode(Element element) {
    Episode episode = new Episode(element);
    
    episodes.add(episode);
    
    return episode;
  }

}
