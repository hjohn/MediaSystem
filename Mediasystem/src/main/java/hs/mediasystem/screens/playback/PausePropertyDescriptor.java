package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.actions.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

public class PausePropertyDescriptor implements PropertyDescriptor<PlayerPresentation> {
  private static final List<Action<PlayerPresentation>> ACTIONS = new ArrayList<>();

  static {
    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.pause";
      }

      @Override
      public String getDescription() {
        return "Toggle Playback Pause";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.pause();
      }
    });
  }

  @Override
  public Class<PlayerPresentation> getPresentationClass() {
    return PlayerPresentation.class;
  }

  @Override
  public List<Action<PlayerPresentation>> getActions() {
    return ACTIONS;
  }
}
