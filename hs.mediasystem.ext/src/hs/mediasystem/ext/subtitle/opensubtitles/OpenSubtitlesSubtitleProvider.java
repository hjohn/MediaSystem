package hs.mediasystem.ext.subtitle.opensubtitles;

import hs.mediasystem.framework.Episode;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.SubtitleProviderException;
import hs.subtitle.SubtitleDescriptor;

import java.util.List;

public class OpenSubtitlesSubtitleProvider implements SubtitleProvider {
  private final OpenSubtitlesClient client;

  public OpenSubtitlesSubtitleProvider() {
    client = new OpenSubtitlesClient("MediaSystem v1");
  }

  @Override
  public List<? extends SubtitleDescriptor> query(MediaItem mediaItem) throws SubtitleProviderException {
    Media media = mediaItem.get(Media.class);
//    Movie movie = mediaItem.get(Movie.class);
    Episode ep = mediaItem.get(Episode.class);

    String title = ep == null ? media.getTitle() : ep.getSerie().getTitle();
    Integer season = ep == null ? null : ep.getSeason();
    Integer episode = ep == null ? null : ep.getEpisode();
    Integer year = media.getReleaseYear();
//    String imdbNumber = movie == null ? null : movie.getImdbNumber();
    String imdbNumber = null;

    System.out.println("[FINE] OpenSubtitlesSubtitleProvider.query() - Looking for subtitles: " + mediaItem.getTitle() + "; " +  year + "; " + season + "; " + episode + "; English");

    try {
      return client.getSubtitleList(imdbNumber, title, season, episode, "eng");
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