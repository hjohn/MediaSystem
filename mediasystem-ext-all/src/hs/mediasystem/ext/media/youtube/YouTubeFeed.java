package hs.mediasystem.ext.media.youtube;

import hs.mediasystem.dao.URLImageSource;
import hs.mediasystem.ext.media.youtube.YouTubeMediaTree.Feed;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.SourceImageHandle;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.ServiceException;

public class YouTubeFeed extends Media implements MediaRoot {
  private static final Id ID = new Id("youTubeFeed");

  private final Feed feed;
  private final YouTubeMediaTree mediaRoot;

  private List<Media> children;

  public YouTubeFeed(YouTubeMediaTree mediaTree, String uri, Feed feed) {
    super(new MediaItem(uri));

    this.externalTitle.set(feed.getName());
    this.mediaRoot = mediaTree;
    this.feed = feed;
  }

  @Override
  public String getRootName() {
    return title.get();
  }

  @Override
  public List<? extends Media> getItems() {
    if(children == null) {
      YouTubeService service = new YouTubeService("MediaSystem");
      String feedUrl = feed.getUrl();
      children = new ArrayList<>();

      try {
        VideoFeed videoFeed = service.getFeed(new URL(feedUrl), VideoFeed.class);

        for(VideoEntry videoEntry : videoFeed.getEntries() ) {
          YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();

          for(YouTubeMediaContent mediaContent : mediaGroup.getYouTubeContents()) {
            YouTubeVideo youTubeVideo = new YouTubeVideo(mediaContent.getUrl().replaceAll("https://", "http://"), videoEntry.getTitle().getPlainText());

            youTubeVideo.description.set(mediaGroup.getDescription().getPlainTextContent());
            if(videoEntry.getRating() != null) {
              youTubeVideo.rating.set(videoEntry.getRating().getAverage() * 2);
            }
            youTubeVideo.releaseDate.set(LocalDateTime.ofEpochSecond(videoEntry.getPublished().getValue(), 0, ZoneOffset.ofTotalSeconds(videoEntry.getPublished().getTzShift() * 60)).toLocalDate());
            youTubeVideo.runtime.set(mediaGroup.getYouTubeContents().get(0).getDuration() / 60);

            List<MediaThumbnail> thumbnails = videoEntry.getMediaGroup().getThumbnails();
            MediaThumbnail bestThumbnail = null;

            for(MediaThumbnail thumbnail : thumbnails) {
              int size = thumbnail.getWidth() * thumbnail.getHeight();

              if(bestThumbnail == null || size > bestThumbnail.getWidth() * bestThumbnail.getHeight()) {
                bestThumbnail = thumbnail;
              }
            }

            String url = bestThumbnail == null ? null : bestThumbnail.getUrl();

            youTubeVideo.image.set(new SourceImageHandle(new URLImageSource(url), "YouTubeMediaTree:/" + videoEntry.getId()));

            children.add(youTubeVideo);
            break;
          }
        }
      }
      catch(ServiceException | IOException e) {
        e.printStackTrace();
      }
    }

    return children;
  }

  public static class YouTubeVideo extends Media {
    public YouTubeVideo(String uri, String title) {
      super(new MediaItem(uri));

      this.externalTitle.set(title);
    }
  }

  @Override
  public Id getId() {
    return ID;
  }

  @Override
  public MediaRoot getParent() {
    return mediaRoot;
  }

  private static final Map<String, Object> MEDIA_PROPERTIES = new HashMap<>();

  static {
    MEDIA_PROPERTIES.put("image.poster", null);
    MEDIA_PROPERTIES.put("image.poster.aspectRatios", new double[] {16.0 / 9.0, 4.0 / 3.0});
    MEDIA_PROPERTIES.put("image.poster.hasIdentifyingTitle", false);
  }

  @Override
  public Map<String, Object> getMediaProperties() {
    return Collections.unmodifiableMap(MEDIA_PROPERTIES);
  }
}
