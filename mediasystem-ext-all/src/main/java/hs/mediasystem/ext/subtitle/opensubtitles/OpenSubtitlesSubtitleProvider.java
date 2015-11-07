package hs.mediasystem.ext.subtitle.opensubtitles;

import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.SubtitleProviderException;
import hs.subtitle.SubtitleDescriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;

@Named
public class OpenSubtitlesSubtitleProvider implements SubtitleProvider {
  private final OpenSubtitlesClient client;

  public OpenSubtitlesSubtitleProvider() {
    client = new OpenSubtitlesClient("MediaSystem v1");
  }

  @Override
  public List<? extends SubtitleDescriptor> query(Map<String, Object> criteria) throws SubtitleProviderException {
    System.out.println("[FINE] OpenSubtitlesSubtitleProvider.query() - Looking for subtitles: " + criteria);

    try {
      return client.getAllSubtitleLists(
        (String)criteria.get(SubtitleCriteriaProvider.IMDB_ID),
        (String)criteria.get(SubtitleCriteriaProvider.TITLE),
        (Integer)criteria.get(SubtitleCriteriaProvider.SEASON),
        (Integer)criteria.get(SubtitleCriteriaProvider.EPISODE),
        (Long)criteria.get(SubtitleCriteriaProvider.OPEN_SUBTITLES_HASH),
        (Long)criteria.get(SubtitleCriteriaProvider.FILE_LENGTH),
        "eng"
      );
    }
    catch(Exception e) {
      throw new SubtitleProviderException(e.getMessage(), e);
    }
  }

  @Override
  public String getName() {
    return "OpenSubtitles";
  }

  @Override
  public Set<String> getMediaTypes() {
    return new HashSet<String>() {{
      add("movie");
      add("episode");
    }};
  }
}