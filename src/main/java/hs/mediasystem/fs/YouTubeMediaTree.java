package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.MediaItem;

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

public class YouTubeMediaTree extends AbstractMediaTree {
  private List<MediaItem> children;

  @Override
  public MediaItem getRoot() {
    return new MediaItem(this, new LocalInfo(MediaType.MOVIE_ROOT, "YouTube")) {
      @Override
      public List<? extends MediaItem> children() {
        if(children == null) {
          YouTubeService service = new YouTubeService("MediaSystem");
          String feedUrl = "https://gdata.youtube.com/feeds/api/standardfeeds/top_rated";
          children = new ArrayList<>();

          try {
            VideoFeed videoFeed = service.getFeed(new URL(feedUrl), VideoFeed.class);

            for(VideoEntry videoEntry : videoFeed.getEntries() ) {
              YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();

              for(YouTubeMediaContent mediaContent : mediaGroup.getYouTubeContents()) {
                children.add(new MediaItem(YouTubeMediaTree.this, new LocalInfo(mediaContent.getUrl().toString(), MediaType.MOVIE, "", videoEntry.getTitle().getPlainText(), null, null, null, null, null)));
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
    };
  }
}
