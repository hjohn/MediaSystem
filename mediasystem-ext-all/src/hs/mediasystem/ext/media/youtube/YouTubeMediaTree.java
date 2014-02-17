package hs.mediasystem.ext.media.youtube;

import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaRoot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

@Named
public class YouTubeMediaTree implements MediaRoot {
  private static final Id ID = new Id("youTubeRoot");
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

  private List<Media> children;

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

  @Override
  public List<? extends Media> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Feed feed : FEEDS) {
        children.add(new YouTubeFeed(YouTubeMediaTree.this, feed.getUrl(), feed));
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "YouTube";
  }

  @Override
  public Id getId() {
    return ID;
  }

  @Override
  public MediaRoot getParent() {
    return null;
  }
}
