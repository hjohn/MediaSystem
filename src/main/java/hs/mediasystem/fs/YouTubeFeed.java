package hs.mediasystem.fs;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.YouTubeMediaTree.Feed;
import hs.mediasystem.media.Media;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.ServiceException;

public class YouTubeFeed extends MediaItem {
  private final Feed feed;

  private List<MediaItem> children;

  public YouTubeFeed(MediaTree mediaTree, String uri, Feed feed, Media media) {
    super(mediaTree, uri, false, media);
    this.feed = feed;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public List<? extends MediaItem> children() {
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

            children.add(new MediaItem(getMediaTree(), mediaContent.getUrl(), false, youTube, videoEntry));
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
}
