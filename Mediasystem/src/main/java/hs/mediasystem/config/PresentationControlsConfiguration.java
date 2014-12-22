package hs.mediasystem.config;

import hs.mediasystem.screens.collection.CollectionPresentation;
import hs.mediasystem.screens.collection.CollectionSelectorPresentation;
import hs.mediasystem.screens.playback.PlaybackOverlayPresentation;
import hs.mediasystem.screens.playback.PlayerPresentation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PresentationControlsConfiguration {
  private static final Map<Class<?>, Map<String, String>> activeActionsByPresentationClass = new HashMap<>();

  static {
    activeActionsByPresentationClass.put(CollectionSelectorPresentation.class, new LinkedHashMap<String, String>() {{
      put("hs.mediasystem.framework.MediaData.viewed", "checkBox");
    }});

    activeActionsByPresentationClass.put(CollectionPresentation.class, new LinkedHashMap<String, String>() {{
      put("hs.mediasystem.screens.collection.CollectionPresentation.groupSet", "comboBox");
      put("hs.mediasystem.screens.collection.CollectionPresentation.layout", "comboBox");
      put("hs.mediasystem.screens.collection.CollectionPresentation.inclusionFilter", "trigger");
    }});

    activeActionsByPresentationClass.put(PlayerPresentation.class, new LinkedHashMap<String, String>() {{
      put("hs.mediasystem.screens.playback.PlayerPresentation.volume", "slider");
      put("hs.mediasystem.screens.playback.PlayerPresentation.brightness", "slider");
      put("hs.mediasystem.screens.playback.PlayerPresentation.rate", "slider");
      put("hs.mediasystem.screens.playback.PlayerPresentation.subtitleDelay", "slider");
      put("hs.mediasystem.screens.playback.PlayerPresentation.audioDelay", "slider");
      put("hs.mediasystem.screens.playback.PlayerPresentation.subtitle", "comboBox");
      put("hs.mediasystem.screens.playback.PlayerPresentation.audioTrack", "comboBox");
    }});

    activeActionsByPresentationClass.put(PlaybackOverlayPresentation.class, new LinkedHashMap<String, String>() {{
      put("hs.mediasystem.screens.playback.PlaybackOverlayPresentation.chooseSubtitle", "trigger");
    }});
  }

  public static Map<Class<?>, Map<String, String>> getActiveActionsByPresentationClass() {
    return activeActionsByPresentationClass;
  }
}
