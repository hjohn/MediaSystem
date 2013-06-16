package hs.mediasystem.ext.media.nos;

import hs.mediasystem.dao.URLImageSource;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.SourceImageHandle;
import hs.mediasystem.persist.PersistQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Named
public class NosMediaTree implements MediaTree, MediaRoot {
  private static final String URL = "http://tv.nos.nl";

  private List<MediaItem> children;

  private static List<MediaItem> getElements() {
    List<MediaItem> list = new ArrayList<>();

    try {
      Document doc = Jsoup.connect(URL).get();

      Map<String, String> videoUrls = new HashMap<>();

      Pattern videoPattern = Pattern.compile("\\$\\(\"#(.*?)\"\\).*?(http://.*?\\.mp4)", Pattern.DOTALL);
      Matcher matcher = videoPattern.matcher(doc.select("script").html());

      while(matcher.find()) {
        videoUrls.put(matcher.group(1), matcher.group(2));
      }

      for(Element element : doc.select("a")) {
        String thumbUrl = URL + "/browser/" + element.select("div img").attr("src");
        String title = element.select("div h3").text();
        String meta = element.select("div p").text();
        String videoUrl = videoUrls.get(element.attr("id"));

        Media<?> media = new NosItem(title, meta);

        media.image.set(new SourceImageHandle(new URLImageSource(thumbUrl), "NosMediaTree:/" + title));

        MediaItem mediaItem = new MediaItem(videoUrl, title, Media.class);

        mediaItem.media.set(media);

        list.add(mediaItem);
      }
    }
    catch(IOException e) {
      System.out.println("[WARN] NosMediaTree.getElements() - Exception occured: " + e);
    }

    return list;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      children = getElements();
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "NOS";
  }

  @Override
  public PersistQueue getPersister() {
    return null;
  }

  @Override
  public String getId() {
    return "nosRoot";
  }

  @Override
  public MediaRoot getParent() {
    return null;
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

  public static class NosItem extends Media<NosItem> {
    public NosItem(String title, String meta) {
      super(title, meta, null);
    }
  }
}
