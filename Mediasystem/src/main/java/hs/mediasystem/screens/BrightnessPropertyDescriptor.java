package hs.mediasystem.screens;

import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.actions.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

public class BrightnessPropertyDescriptor implements PropertyDescriptor<PlayerPresentation> {
  private static final List<Action<PlayerPresentation>> ACTIONS = new ArrayList<>();

  static {
    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.brightness.decrease(5%)";
      }

      @Override
      public String getDescription() {
        return "Decrease Brightness by 5%";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.changeBrightness(-0.05f);
      }
    });

    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.brightness.increase(5%)";
      }

      @Override
      public String getDescription() {
        return "Increase Brightness by 5%";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.changeBrightness(0.05f);
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
