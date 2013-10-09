package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.actions.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

public class OverlayVisibilityPropertyDescriptor implements PropertyDescriptor<PlaybackOverlayPresentation> {
  private static final List<Action<PlaybackOverlayPresentation>> ACTIONS = new ArrayList<>();

  static {
    ACTIONS.add(new Action<PlaybackOverlayPresentation>() {
      @Override
      public String getId() {
        return "playback.overlay.visibility";
      }

      @Override
      public String getDescription() {
        return "Toggle Playback Overlay Visibility";
      }

      @Override
      public void perform(PlaybackOverlayPresentation presentation) {
        presentation.overlayVisible.set(!presentation.overlayVisible.get());
      }
    });
  }

  @Override
  public Class<PlaybackOverlayPresentation> getPresentationClass() {
    return PlaybackOverlayPresentation.class;
  }

  @Override
  public List<Action<PlaybackOverlayPresentation>> getActions() {
    return ACTIONS;
  }
}
