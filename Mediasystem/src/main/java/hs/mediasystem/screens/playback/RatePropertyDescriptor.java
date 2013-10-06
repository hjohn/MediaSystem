package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.actions.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

public class RatePropertyDescriptor implements PropertyDescriptor<PlayerPresentation> {
  private static final List<Action<PlayerPresentation>> ACTIONS = new ArrayList<>();

  static {
    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.rate.decrease(10%)";
      }

      @Override
      public String getDescription() {
        return "Decrease Playback Rate by 10%";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.changeRate(-0.1f);
      }
    });

    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.rate.increase(10%)";
      }

      @Override
      public String getDescription() {
        return "Increase Playback Rate by 10%";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.changeRate(0.1f);
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
