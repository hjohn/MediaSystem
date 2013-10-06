package hs.mediasystem.screens;

import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.actions.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

public class VolumePropertyDescriptor implements PropertyDescriptor<PlayerPresentation> {
  private static final List<Action<PlayerPresentation>> ACTIONS = new ArrayList<>();

  static {
    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.volume.decrease(5%)";
      }

      @Override
      public String getDescription() {
        return "Decrease Playback Volume by 5%";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.changeVolume(-5);
      }
    });

    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.volume.increase(5%)";
      }

      @Override
      public String getDescription() {
        return "Increase Playback Volume by 5%";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.changeVolume(5);
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
