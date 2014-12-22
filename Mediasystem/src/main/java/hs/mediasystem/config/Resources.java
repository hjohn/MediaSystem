package hs.mediasystem.config;

import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Resources {
  public static ResourceBundle resourceBundle = new ListResourceBundle() {
    @Override
    protected Object[][] getContents() {
      return new Object[][] {
        {"hs.mediasystem.framework.MediaData.viewed:checkBox.label", "Viewed"},
        {"hs.mediasystem.screens.collection.CollectionPresentation.inclusionFilter:trigger.label", "Filter..."},
        {"hs.mediasystem.screens.collection.CollectionPresentation.layout:comboBox.label", "Layout"},
        {"hs.mediasystem.screens.collection.CollectionPresentation.groupSet:comboBox.label", "Sorting & Grouping"},
        {"hs.mediasystem.ext.media.movie.AlphaGroupByTitleMediaGroup:label", "Alphabetically, grouped by Title"},
        {"hs.mediasystem.ext.media.movie.AlphaMediaGroup:label", "Alphabetically"},
        {"hs.mediasystem.ext.media.movie.GenreMediaGroup:label", "Alphabetically, grouped by Genre"},
        {"hs.mediasystem.ext.screens.collection.tree.DetailAndListLayout:label", "Detail and List"},
        {"hs.mediasystem.ext.screens.collection.carousel.CarouselLayout:label", "Carousel"},
        {"hs.mediasystem.ext.screens.collection.tree.ListAndDetailLayout:label", "List and Detail"},
        {"hs.mediasystem.ext.screens.collection.banner.DetailAndBannersLayout:label", "Detail and Banners"},
        {"hs.mediasystem.screens.playback.PlayerPresentation.volume:slider.label", "Volume"},
        {"hs.mediasystem.screens.playback.PlayerPresentation.brightness:slider.label", "Brightness"},
        {"hs.mediasystem.screens.playback.PlayerPresentation.rate:slider.label", "Playback Rate"},
        {"hs.mediasystem.screens.playback.PlayerPresentation.subtitleDelay:slider.label", "Subtitle Delay"},
        {"hs.mediasystem.screens.playback.PlayerPresentation.subtitle:comboBox.label", "Subtitle"},
        {"hs.mediasystem.screens.playback.PlaybackOverlayPresentation.chooseSubtitle:trigger.label", "Choose Subtitle..."},
        {"hs.mediasystem.screens.playback.PlayerPresentation.audioDelay:slider.label", "Audio Delay"},
        {"hs.mediasystem.screens.playback.PlayerPresentation.audioTrack:comboBox.label", "Audio Track"}
      };
    }
  };

  public static String getResource(String propertyName, String key) {
    try {
      return resourceBundle.getString(propertyName + ":" + key);
    }
    catch(MissingResourceException e) {
      System.out.println("[WARN] Resource missing for key: " + propertyName + ":" + key);
      return "<" + propertyName.substring(propertyName.lastIndexOf(".") + 1) + "." + key + ">";
    }
  }
}
