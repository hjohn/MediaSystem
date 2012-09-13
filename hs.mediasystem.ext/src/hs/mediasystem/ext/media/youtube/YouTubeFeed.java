package hs.mediasystem.ext.media.youtube;

import hs.mediasystem.dao.URLImageSource;
import hs.mediasystem.ext.media.youtube.YouTubeMediaTree.Feed;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.SourceImageHandle;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.ServiceException;

public class YouTubeFeed extends MediaItem implements MediaRoot {
  private final Feed feed;
  private final YouTubeMediaTree mediaRoot;

  private List<MediaItem> children;

  public YouTubeFeed(YouTubeMediaTree mediaTree, String uri, Feed feed, Media<?> media) {
    super(uri, media);

    this.mediaRoot = mediaTree;
    this.feed = feed;
  }

  @Override
  public String getRootName() {
    return getTitle();
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      YouTubeService service = new YouTubeService("MediaSystem");
      String feedUrl = feed.getUrl();
      children = new ArrayList<>();

      try {
        VideoFeed videoFeed = service.getFeed(new URL(feedUrl), VideoFeed.class);

        for(VideoEntry videoEntry : videoFeed.getEntries() ) {
          YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();

          for(YouTubeMediaContent mediaContent : mediaGroup.getYouTubeContents()) {
            YouTubeVideo youTube = new YouTubeVideo(videoEntry.getTitle().getPlainText());

            youTube.description.set(mediaGroup.getDescription().getPlainTextContent());
            if(videoEntry.getRating() != null) {
              youTube.rating.set(videoEntry.getRating().getAverage() * 2);
            }
            youTube.releaseDate.set(new Date(videoEntry.getPublished().getValue()));
            youTube.runtime.set(mediaGroup.getYouTubeContents().get(0).getDuration() / 60);

            List<MediaThumbnail> thumbnails = videoEntry.getMediaGroup().getThumbnails();
            MediaThumbnail bestThumbnail = null;

            for(MediaThumbnail thumbnail : thumbnails) {
              int size = thumbnail.getWidth() * thumbnail.getHeight();

              if(bestThumbnail == null || size > bestThumbnail.getWidth() * bestThumbnail.getHeight()) {
                bestThumbnail = thumbnail;
              }
            }

            String url = bestThumbnail == null ? null : bestThumbnail.getUrl();

            youTube.image.set(new SourceImageHandle(new URLImageSource(url), "YouTubeMediaTree:/" + videoEntry.getId()));

            children.add(new MediaItem(mediaContent.getUrl().replaceAll("https://", "http://"), youTube));
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

  public static class YouTubeVideo extends Media<YouTubeVideo> {
    public YouTubeVideo(String title) {
      super(title);
    }
  }

  @Override
  public String getId() {
    return "youtubeFeed[" + feed.getName() + "]";
  }

  @Override
  public MediaRoot getParent() {
    return mediaRoot;
  }
}
