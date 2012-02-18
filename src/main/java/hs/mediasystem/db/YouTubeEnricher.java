package hs.mediasystem.db;

import java.util.Date;
import java.util.List;

import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;

public class YouTubeEnricher implements ItemEnricher<VideoEntry> {

  @Override
  public String getProviderCode() {
    return "YOUTUBE";
  }

  @Override
  public String identifyItem(LocalInfo<VideoEntry> localInfo) throws IdentifyException {
    System.out.println(">>> identifyItem " + localInfo.getCode());
    return localInfo.getCode();
  }

  @Override
  public Item loadItem(String identifier, LocalInfo<VideoEntry> localInfo) throws ItemNotFoundException {
    System.out.println(">>> loadItem " + identifier);
    VideoEntry entry = localInfo.getUserData();

    List<MediaThumbnail> thumbnails = entry.getMediaGroup().getThumbnails();
    MediaThumbnail bestThumbnail = null;

    for(MediaThumbnail thumbnail : thumbnails) {
      int size = thumbnail.getWidth() * thumbnail.getHeight();

      if(bestThumbnail == null || size > bestThumbnail.getWidth() * bestThumbnail.getHeight()) {
        bestThumbnail = thumbnail;
      }
    }

    String url = bestThumbnail == null ? null : bestThumbnail.getUrl();

    final byte[] poster = url != null ? Downloader.tryReadURL(url) : null;

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

    item.setBackground(Item.NULL);
    item.setBanner(Item.NULL);
    item.setPoster(new MemorySource<>(poster));

    return item;
  }

}
