package hs.mediasystem.fs;

import hs.mediasystem.db.URLImageSource;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.persist.Persister;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class NosMediaTree implements MediaTree, MediaRoot {
  private static final String URL = "http://tv.nos.nl";

  private List<MediaItem> children;

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
            Media media = new Media(title, meta, null);

            media.imageProperty().set(new SourceImageHandle(new URLImageSource(thumbUrl), "NosMediaTree:/" + title));

            MediaItem mediaItem = new MediaItem(this, matcher2.group(0), media);

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
  public EnrichCache getEnrichCache() {
    return null;
  }

  @Override
  public Persister getPersister() {
    return null;
  }
}
