package hs.mediasystem.framework;

import hs.subtitle.SubtitleDescriptor;
import hs.subtitle.opensub.OpenSubtitlesClient;

import java.util.List;

public class OpenSubtitlesSubtitleProvider implements SubtitleProvider {
  private final OpenSubtitlesClient client;

  public OpenSubtitlesSubtitleProvider(String clientIdentity) {
    client = new OpenSubtitlesClient(clientIdentity);
  }

  @Override
  public List<? extends SubtitleDescriptor> query(MediaItem mediaItem) throws SubtitleProviderException {
    Integer year = mediaItem.getReleaseYear();

    Integer season = mediaItem.getSeason();
    Integer episode = mediaItem.getEpisode();

    if(season == null) {
      episode = null;
    }

    System.out.println("[FINE] OpenSubtitlesSubtitleProvider.query() - Looking for subtitles: " + mediaItem.getTitle() + "; " +  year + "; " + season + "; " + episode + "; English");

    try {
      return client.getSubtitleList(mediaItem.getImdbId(), season != null ? mediaItem.getGroupName() : mediaItem.getTitle(), season, episode, "eng");
    }
    catch(Exception e) {
      throw new SubtitleProviderException(e.getMessage(), e);
    }
  }

  @Override
  public String getName() {
    return "OpenSubtitles";
  }
}