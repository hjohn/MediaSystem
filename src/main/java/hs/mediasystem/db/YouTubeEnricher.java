package hs.mediasystem.db;

import hs.mediasystem.framework.MediaItem;

import java.util.Date;
import java.util.List;

import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;

public class YouTubeEnricher implements ItemEnricher {

  @Override
  public String getProviderCode() {
    return "YOUTUBE";
  }

  @Override
  public String identifyItem(MediaItem mediaItem) throws IdentifyException {
    System.out.println(">>> identifyItem " + mediaItem.get(VideoEntry.class).getId());
    return mediaItem.get(VideoEntry.class).getId();
  }

  @Override
  public Item loadItem(String identifier, MediaItem mediaItem) throws ItemNotFoundException {
    System.out.println(">>> loadItem " + identifier);
    VideoEntry entry = mediaItem.get(VideoEntry.class);

    List<MediaThumbnail> thumbnails = entry.getMediaGroup().getThumbnails();
    MediaThumbnail bestThumbnail = null;

    for(MediaThumbnail thumbnail : thumbnails) {
      int size = thumbnail.getWidth() * thumbnail.getHeight();

      if(bestThumbnail == null || size > bestThumbnail.getWidth() * bestThumbnail.getHeight()) {
        bestThumbnail = thumbnail;
      }
    }

    String url = bestThumbnail == null ? null : bestThumbnail.getUrl();

    YouTubeMediaGroup mediaGroup = entry.getMediaGroup();

    Item item = new Item();

    item.setTitle(entry.getTitle().getPlainText());
    item.setPlot(mediaGroup.getDescription().getPlainTextContent());
    item.setRating(entry.getRating().getAverage() * 2);
    item.setReleaseDate(new Date(entry.getPublished().getValue()));
    item.setRuntime(mediaGroup.getYouTubeContents().get(0).getDuration() / 60);
//    item.setTagline();
//    item.setLanguage();

//    YtStatistics stats = entry.getStatistics();
//
//    if(stats != null) {
//      stats.getViewCount();
//    }

    item.setBackgroundURL(null);
    item.setBannerURL(null);
    item.setPosterURL(url);

    return item;
  }

}
