package hs.mediasystem.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hs.mediasystem.Component;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Provides information about key mappings.
 */
@Component
public class KeyMappingsConfiguration {
  private static final Map<KeyCodeCombination, List<String>> actionsByKeyCodeCombination = new HashMap<>();

  static {
    add(new KeyCodeCombination(KeyCode.V), "hs.mediasystem.framework.MediaData.viewed:toggle");
    add(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN), "hs.mediasystem.screens.collection.CollectionPresentation.groupSet:next");
    add(new KeyCodeCombination(KeyCode.F), "hs.mediasystem.screens.collection.CollectionPresentation.inclusionFilter:trigger");

    // PlayerPresentation
    add(new KeyCodeCombination(KeyCode.LEFT), "hs.mediasystem.screens.playback.PlayerPresentation.position:subtract(10000)");
    add(new KeyCodeCombination(KeyCode.RIGHT), "hs.mediasystem.screens.playback.PlayerPresentation.position:add(10000)");
    add(new KeyCodeCombination(KeyCode.DOWN), "hs.mediasystem.screens.playback.PlayerPresentation.position:subtract(60000)");
    add(new KeyCodeCombination(KeyCode.UP), "hs.mediasystem.screens.playback.PlayerPresentation.position:add(60000)");
    add(new KeyCodeCombination(KeyCode.NUMPAD4), "hs.mediasystem.screens.playback.PlayerPresentation.position:subtract(10000)");
    add(new KeyCodeCombination(KeyCode.NUMPAD6), "hs.mediasystem.screens.playback.PlayerPresentation.position:add(10000)");
    add(new KeyCodeCombination(KeyCode.NUMPAD2), "hs.mediasystem.screens.playback.PlayerPresentation.position:subtract(60000)");
    add(new KeyCodeCombination(KeyCode.NUMPAD8), "hs.mediasystem.screens.playback.PlayerPresentation.position:add(60000)");
    add(new KeyCodeCombination(KeyCode.SPACE), "hs.mediasystem.screens.playback.PlayerPresentation.paused:toggle");
    add(new KeyCodeCombination(KeyCode.M), "hs.mediasystem.screens.playback.PlayerPresentation.muted:toggle");
    add(new KeyCodeCombination(KeyCode.DIGIT9), "hs.mediasystem.screens.playback.PlayerPresentation.volume:subtract(5)");
    add(new KeyCodeCombination(KeyCode.DIGIT0), "hs.mediasystem.screens.playback.PlayerPresentation.volume:add(5)");
    add(new KeyCodeCombination(KeyCode.DIGIT1), "hs.mediasystem.screens.playback.PlayerPresentation.brightness:subtract(0.05)");
    add(new KeyCodeCombination(KeyCode.DIGIT2), "hs.mediasystem.screens.playback.PlayerPresentation.brightness:add(0.05)");
    add(new KeyCodeCombination(KeyCode.OPEN_BRACKET), "hs.mediasystem.screens.playback.PlayerPresentation.rate:subtract(0.1)");
    add(new KeyCodeCombination(KeyCode.CLOSE_BRACKET), "hs.mediasystem.screens.playback.PlayerPresentation.rate:add(0.1)");
    add(new KeyCodeCombination(KeyCode.Z), "hs.mediasystem.screens.playback.PlayerPresentation.subtitleDelay:subtract(100)");
    add(new KeyCodeCombination(KeyCode.X), "hs.mediasystem.screens.playback.PlayerPresentation.subtitleDelay:add(100)");
    add(new KeyCodeCombination(KeyCode.J), "hs.mediasystem.screens.playback.PlayerPresentation.subtitle:next");

    add(new KeyCodeCombination(KeyCode.I), "hs.mediasystem.screens.collection.CollectionSelectorPresentation.showInformationDialog:trigger",
                                           "hs.mediasystem.screens.playback.PlaybackOverlayPresentation.overlayVisible:toggle");
  }

  private static void add(KeyCodeCombination keyCodeCombination, String... actions) {
    actionsByKeyCodeCombination.put(keyCodeCombination, Arrays.asList(actions));
  }

  public Map<KeyCodeCombination, List<String>> getNewKeyMappings() {
    return actionsByKeyCodeCombination;
  }
}
