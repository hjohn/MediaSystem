package hs.mediasystem.framework;

import hs.sublight.SublightSubtitleClient;
import hs.sublight.SubtitleDescriptor;

import java.util.List;

public class SublightSubtitleProvider implements SubtitleProvider {
  private final SublightSubtitleClient client;

  public SublightSubtitleProvider(String clientIdentity, String apiKey) {
    client = new SublightSubtitleClient(clientIdentity, apiKey);
  }
  
  @Override
  public List<SubtitleDescriptor> query(MediaItem mediaItem) {
    Integer year = null;
    
    try {
      year = Integer.parseInt(mediaItem.getReleaseYear());
    }
    catch(NumberFormatException e) {
    }

    Short season = (short)mediaItem.getSeason(); 
    Integer episode = mediaItem.getEpisode();
    
    if(season == 0) {
      season = null;
      episode = null;
    }

    System.out.println("Looking for subtitles: " + mediaItem.getTitle() + "; " +  year + "; " + season + "; " + episode + "; English");

    return client.getSubtitleList(mediaItem.getTitle(), year, season, episode, "English");
  }

  @Override
  public String getName() {
    return "Sublight";
  }
}
