package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.actions.Action;

import java.util.function.Consumer;

public enum PlayerActions implements Action<PlayerPresentation> {
  BRIGHTNESS_INCREASE(presentation -> presentation.changeBrightness(0.05f)),
  BRIGHTNESS_DECREASE(presentation -> presentation.changeBrightness(-0.05f)),
  MUTE(presentation -> presentation.mute()),
  PAUSE(presentation -> presentation.pause()),
  RATE_INCREASE(presentation -> presentation.changeRate(0.1f)),
  RATE_DECREASE(presentation -> presentation.changeRate(-0.1f)),
  SKIP_FORWARD_10S(presentation -> presentation.move(10 * 1000)),
  SKIP_BACKWARD_10S(presentation -> presentation.move(-10 * 1000)),
  SKIP_FORWARD_60S(presentation -> presentation.move(60 * 1000)),
  SKIP_BACKWARD_60S(presentation -> presentation.move(-60 * 1000)),
  SUBTITLE_DELAY_INCREASE(presentation -> presentation.changeSubtitleDelay(100)),
  SUBTITLE_DELAY_DECREASE(presentation -> presentation.changeSubtitleDelay(-100)),
  SUBTITLE_NEXT(presentation -> presentation.nextSubtitle()),
  VOLUME_INCREASE(presentation -> presentation.changeVolume(5)),
  VOLUME_DECREASE(presentation -> presentation.changeVolume(-5));

  private final Consumer<PlayerPresentation> action;

  PlayerActions(Consumer<PlayerPresentation> action) {
    this.action = action;
  }

  @Override
  public void perform(PlayerPresentation presentation) {
    action.accept(presentation);
  }
}
