package hs.mediasystem.screens;

import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.actions.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

public class PositionPropertyDescriptor implements PropertyDescriptor<PlayerPresentation> {
  private static final List<Action<PlayerPresentation>> ACTIONS = new ArrayList<>();

  static {
    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.position.backward(10s)";
      }

      @Override
      public String getDescription() {
        return "Jump Backwards by 10 seconds";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.move(-10 * 1000);
      }
    });

    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.position.backward(60s)";
      }

      @Override
      public String getDescription() {
        return "Jump Backwards by 60 seconds";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.move(-60 * 1000);
      }
    });

    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.position.forward(10s)";
      }

      @Override
      public String getDescription() {
        return "Jump Forwards by 10 seconds";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.move(10 * 1000);
      }
    });

    ACTIONS.add(new Action<PlayerPresentation>() {
      @Override
      public String getId() {
        return "playback.position.forward(60s)";
      }

      @Override
      public String getDescription() {
        return "Jump Forwards by 60 seconds";
      }

      @Override
      public void perform(PlayerPresentation presentation) {
        presentation.move(60 * 1000);
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
