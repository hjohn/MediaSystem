package hs.mediasystem.framework;

import hs.subtitle.SubtitleDescriptor;
import hs.subtitle.sublight.SublightSubtitleClient;

import java.util.List;

public class SublightSubtitleProvider implements SubtitleProvider {
  private final SublightSubtitleClient client;

  public SublightSubtitleProvider(String clientIdentity, String apiKey) {
    client = new SublightSubtitleClient(clientIdentity, apiKey);
  }

  @Override
  public List<SubtitleDescriptor> query(MediaItem mediaItem) {
    Integer year = mediaItem.getReleaseYear();

    Short season = mediaItem.getSeason() == null ? null : mediaItem.getSeason().shortValue();
    Integer episode = mediaItem.getEpisode();

    if(season == null) {
      episode = null;
    }
    System.out.println("[FINE] SublightSubtitleProvider.query() - Looking for subtitles: " + mediaItem.getTitle() + "; " +  year + "; " + season + "; " + episode + "; English");

    return client.getSubtitleList(mediaItem.getTitle(), year, season, episode, "English");
  }

  @Override
  public String getName() {
    return "Sublight";
  }
}
