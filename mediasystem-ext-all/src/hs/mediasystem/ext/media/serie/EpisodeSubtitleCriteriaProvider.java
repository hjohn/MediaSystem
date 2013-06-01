package hs.mediasystem.ext.media.serie;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleCriteriaProvider;

@Named
public class EpisodeSubtitleCriteriaProvider implements SubtitleCriteriaProvider {

  @Override
  public Map<String, Object> getCriteria(MediaItem mediaItem) {
    Media<?> media = mediaItem.getMedia();
    MediaData mediaData = mediaItem.mediaData.get();

    Map<String, Object> criteria = new HashMap<>();

    if(media instanceof Episode) {
      Episode ep = (Episode)media;

      criteria.put(SubtitleCriteriaProvider.TITLE, ep.serie.get().title.get());
      criteria.put(SubtitleCriteriaProvider.SEASON, ep.season.get());
      criteria.put(SubtitleCriteriaProvider.EPISODE, ep.episode.get());
    }

    if(mediaData != null) {
      criteria.put(SubtitleCriteriaProvider.OPEN_SUBTITLES_HASH, mediaData.osHash.get());
      criteria.put(SubtitleCriteriaProvider.FILE_LENGTH, mediaData.fileLength.get());
    }

    return criteria;
  }

  @Override
  public Class<?> getMediaType() {
    return Episode.class;
  }
}
