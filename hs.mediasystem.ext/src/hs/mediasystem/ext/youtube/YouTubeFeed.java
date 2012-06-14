package hs.mediasystem.ext.youtube;

import hs.mediasystem.dao.URLImageSource;
import hs.mediasystem.ext.youtube.YouTubeMediaTree.Feed;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.SourceImageHandle;

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

  private List<MediaItem> children;

  public YouTubeFeed(MediaTree mediaTree, String uri, Feed feed, Media media) {
    super(mediaTree, uri, media);
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

            youTube.descriptionProperty().set(mediaGroup.getDescription().getPlainTextContent());
            if(videoEntry.getRating() != null) {
              youTube.ratingProperty().set(videoEntry.getRating().getAverage() * 2);
            }
            youTube.releaseDateProperty().set(new Date(videoEntry.getPublished().getValue()));
            youTube.runtimeProperty().set(mediaGroup.getYouTubeContents().get(0).getDuration() / 60);

            List<MediaThumbnail> thumbnails = videoEntry.getMediaGroup().getThumbnails();
            MediaThumbnail bestThumbnail = null;

            for(MediaThumbnail thumbnail : thumbnails) {
              int size = thumbnail.getWidth() * thumbnail.getHeight();

              if(bestThumbnail == null || size > bestThumbnail.getWidth() * bestThumbnail.getHeight()) {
                bestThumbnail = thumbnail;
              }
            }

            String url = bestThumbnail == null ? null : bestThumbnail.getUrl();

            youTube.imageProperty().set(new SourceImageHandle(new URLImageSource(url), "YouTubeMediaTree:/" + videoEntry.getId()));

            children.add(new MediaItem(getMediaTree(), mediaContent.getUrl().replaceAll("https://", "http://"), youTube, videoEntry));
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
    public YouTubeVideo(String title) {
      super(title);
    }
  }

  @Override
  public String getId() {
    return "youtubeFeed[" + feed.getName() + "]";
  }
}
