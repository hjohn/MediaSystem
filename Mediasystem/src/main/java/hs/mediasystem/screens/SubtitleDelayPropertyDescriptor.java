package hs.mediasystem.screens;

import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.actions.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

public class SubtitleDelayPropertyDescriptor implements PropertyDescriptor<PlayerPresentation> {
  private static final List<Action<PlayerPresentation>> ACTIONS = new ArrayList<>();

  static {
    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.subtitleDelay.decrease(100ms)";
      }

      @Override
      public String getDescription() {
        return "Decrease Subtitle Delay by 100 milliseconds";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.changeSubtitleDelay(-100);
      }
    });

    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.subtitleDelay.increase(100ms)";
      }

      @Override
      public String getDescription() {
        return "Increase Subtitle Delay by 100 milliseconds";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.changeSubtitleDelay(100);
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
