package hs.mediasystem.ext.subtitle.sublight;

import hs.mediasystem.framework.Episode;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.SubtitleProviderException;
import hs.mediasystem.util.CryptoUtil;
import hs.subtitle.SubtitleDescriptor;

import java.util.List;

public class SublightSubtitleProvider implements SubtitleProvider {
  private final SublightSubtitleClient client;

  public SublightSubtitleProvider() {
    String clientIdentity = CryptoUtil.decrypt("EBC4D196C84FF9CB52654303EDB969283377011F640F89D3D1F8527F540CF940", "-MediaSystem-");
    String apiKey = CryptoUtil.decrypt("F75D6CE82F7EBFADF37EF4956905386C99F344CB5E6B592AC8858111AE721DBDFB2E30CF7DF05228C16BDDAB9ABE1F63", "-MediaSystem-");

    client = new SublightSubtitleClient(clientIdentity, apiKey);
  }

  @Override
  public List<SubtitleDescriptor> query(MediaItem mediaItem) throws SubtitleProviderException {
    Media media = mediaItem.get(Media.class);
    Episode ep = mediaItem.get(Episode.class);

    String title = ep == null ? media.getTitle() : ep.getSerie().getTitle();
    Short season = ep == null ? null : ep.getSeason().shortValue();
    Integer episode = ep == null ? null : ep.getEpisode();
    Integer year = media.getReleaseYear();

    System.out.println("[FINE] SublightSubtitleProvider.query() - Looking for subtitles: " + title + "; " +  year + "; " + season + "; " + episode + "; English");

    try {
      return client.getSubtitleList(title, year, season, episode, "English");
    }
    catch(RuntimeException e) {
      throw new SubtitleProviderException(e.getMessage(), e);
    }
  }

  @Override
  public String getName() {
    return "Sublight";
  }
}
