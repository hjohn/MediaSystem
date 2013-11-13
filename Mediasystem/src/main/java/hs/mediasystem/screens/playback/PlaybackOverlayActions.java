package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.actions.PresentationActionEvent;

import javafx.event.EventHandler;

public enum PlaybackOverlayActions implements EventHandler<PresentationActionEvent<PlaybackOverlayPresentation>> {
  VISIBILITY(event -> event.getPresentation().overlayVisible.set(!event.getPresentation().overlayVisible.get()));

  private final EventHandler<PresentationActionEvent<PlaybackOverlayPresentation>> eventHandler;

  PlaybackOverlayActions(EventHandler<PresentationActionEvent<PlaybackOverlayPresentation>> eventHandler) {
    this.eventHandler = eventHandler;
  }

  @Override
  public void handle(PresentationActionEvent<PlaybackOverlayPresentation> event) {
    eventHandler.handle(event);
  }
}
