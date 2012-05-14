package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class YouTubeMediaTree extends AbstractMediaTree {
  private static final List<Feed> FEEDS = new ArrayList<>();

  static {
    FEEDS.add(new Feed("Most Viewed", "https://gdata.youtube.com/feeds/api/standardfeeds/most_viewed"));
    FEEDS.add(new Feed("Top Rated", "https://gdata.youtube.com/feeds/api/standardfeeds/top_rated"));
    FEEDS.add(new Feed("Recently Featured", "https://gdata.youtube.com/feeds/api/standardfeeds/recently_featured"));
    FEEDS.add(new Feed("Most Discussed", "https://gdata.youtube.com/feeds/api/standardfeeds/most_discussed"));
    FEEDS.add(new Feed("Top Favourites", "https://gdata.youtube.com/feeds/api/standardfeeds/top_favorites"));
    FEEDS.add(new Feed("Most Responded", "https://gdata.youtube.com/feeds/api/standardfeeds/most_responded"));
    FEEDS.add(new Feed("Most Recent", "https://gdata.youtube.com/feeds/api/standardfeeds/most_recent"));
    FEEDS.add(new Feed("Most Recent Comedy", "https://gdata.youtube.com/feeds/api/standardfeeds/most_recent_Comedy"));
  }

  private List<MediaItem> children;

  @Override
  public MediaItem getRoot() {
    return new MediaItem(this, new LocalInfo<>("http://youtube.com", "YOUTUBE_ROOT", "YouTube")) {
      @Override
      public List<? extends MediaItem> children() {
        if(children == null) {
          children = new ArrayList<>();

          for(Feed feed : FEEDS) {
            children.add(new YouTubeFeed(YouTubeMediaTree.this, new LocalInfo<>(feed.getUrl(), "YOUTUBE_FEED", feed.getName()), feed));
          }
        }

        return children;
      }
    };
  }

  public static class Feed {
    private final String name;
    private final String url;

    public Feed(String name, String url) {
      this.name = name;
      this.url = url;
    }

    public String getName() {
      return name;
    }

    public String getUrl() {
      return url;
    }
  }
}
