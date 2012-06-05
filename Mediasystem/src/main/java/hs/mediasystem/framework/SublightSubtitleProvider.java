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
    Media media = mediaItem.get(Media.class);
    Episode ep = mediaItem.get(Episode.class);

    String title = ep == null ? media.getTitle() : ep.getSerie().getTitle();
    Short season = ep == null ? null : ep.getSeason().shortValue();
    Integer episode = ep == null ? null : ep.getEpisode();
    Integer year = media.getReleaseYear();

    System.out.println("[FINE] SublightSubtitleProvider.query() - Looking for subtitles: " + title + "; " +  year + "; " + season + "; " + episode + "; English");

    return client.getSubtitleList(title, year, season, episode, "English");
  }

  @Override
  public String getName() {
    return "Sublight";
  }
}
