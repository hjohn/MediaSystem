package hs.mediasystem.ext.media.nos;

import hs.mediasystem.dao.URLImageSource;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.SourceImageHandle;
import hs.mediasystem.framework.descriptors.AbstractEntityDescriptors;
import hs.mediasystem.framework.descriptors.Descriptor;
import hs.mediasystem.framework.descriptors.DescriptorSet;
import hs.mediasystem.framework.descriptors.DescriptorSet.Attribute;
import hs.mediasystem.framework.descriptors.EntityDescriptors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@Named
public class NosMediaTree implements MediaRoot {
  private static final Id ID = new Id("nosRoot");
  private static final String URL = "http://tv.nos.nl";

  private List<Media> children;

  private static List<Media> getElements() {
    List<Media> list = new ArrayList<>();

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

        Media media = new NosItem(videoUrl, title, meta);

        media.image.set(new SourceImageHandle(new URLImageSource(thumbUrl), "NosMediaTree:/" + title));

        list.add(media);
      }
    }
    catch(IOException e) {
      System.out.println("[WARN] NosMediaTree.getElements() - Exception occured: " + e);
    }

    return list;
  }

  @Override
  public List<? extends Media> getItems() {
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
  public Id getId() {
    return ID;
  }

  @Override
  public MediaRoot getParent() {
    return null;
  }

  public static class NosItem extends Media {
    private static final EntityDescriptors DESCRIPTORS = new AbstractEntityDescriptors() {{
      initializeDescriptors(
        new Descriptor("title", 0.99999, new TextType(TextType.SubType.STRING, TextType.Size.TITLE))
      );

      initializeSets(
        new DescriptorSet(
          ImmutableList.of(getDescriptor("title")),
          ImmutableSet.of(Attribute.SORTABLE, Attribute.PREFERRED, Attribute.CONCISE)
        )
      );
    }};

    public NosItem(String videoUrl, String title, String meta) {
      super(DESCRIPTORS, new MediaItem(videoUrl));

      this.externalTitle.set(title);
      this.subtitle.set(meta);
    }
  }
}
