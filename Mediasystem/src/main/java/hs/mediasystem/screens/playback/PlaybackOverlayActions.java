package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.actions.Action;

import java.util.function.Consumer;

public enum PlaybackOverlayActions implements Action<PlaybackOverlayPresentation> {
  VISIBILITY(presentation -> presentation.overlayVisible.set(!presentation.overlayVisible.get()));

  private final Consumer<PlaybackOverlayPresentation> action;

  PlaybackOverlayActions(Consumer<PlaybackOverlayPresentation> action) {
    this.action = action;
  }

  @Override
  public void perform(PlaybackOverlayPresentation presentation) {
    action.accept(presentation);
  }
}
