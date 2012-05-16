package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.URLImageSource;
import hs.mediasystem.framework.MediaItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class NosMediaTree extends AbstractMediaTree {
  private static final String URL = "http://tv.nos.nl";

  private List<MediaItem> children;

  @Override
  public MediaItem getRoot() {
    return new MediaItem(this, new LocalInfo<>(URL, "NOS_ROOT", "NOS"), false) {
      @Override
      public List<? extends MediaItem> children() {
        if(children == null) {
          children = getElements();
        }

        return children;
      }
    };
  }

  private List<MediaItem> getElements() {
    List<MediaItem> list = new ArrayList<>();

    try {
      Document doc = Jsoup.connect(URL).get();

      for(Element element : doc.select("a")) {
        String videoUrl = URL + "/browser/" + element.attr("href");
        String thumbUrl = URL + "/browser/" + element.select("div img").attr("src");
        String title = element.select("div h3").text();
        String meta = element.select("div p").text();

        Document vidXml = Jsoup.connect(videoUrl).get();

        Pattern pattern = Pattern.compile("http://content.nos.nl/.*?\\.xml");
        Matcher matcher = pattern.matcher(vidXml.toString());

        if(matcher.find()) {
          String videoXmlUrl = matcher.group(0);

          Document vid = Jsoup.connect(videoXmlUrl).get();
          Pattern pattern2 = Pattern.compile("http://.*?\\.(flv|mp4)");
          Matcher matcher2 = pattern2.matcher(vid.toString());

          if(matcher2.find()) {
            MediaItem mediaItem = new MediaItem(this, new LocalInfo<>(matcher2.group(0), "NOS", null, title, meta, null, null, null, null, null, null), false);

            mediaItem.posterProperty().set(new SourceImageHandle(new URLImageSource(thumbUrl), "NosMediaTree:/" + mediaItem.getTitle()));

            list.add(mediaItem);
          }
        }
      }
    }
    catch(IOException e) {
      System.out.println("[WARN] NosMediaTree.getElements() - Exception occured: " + e);
    }

    return list;
  }
}
