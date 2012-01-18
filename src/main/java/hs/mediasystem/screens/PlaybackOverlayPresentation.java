package hs.mediasystem.screens;

import javax.inject.Inject;

public class PlaybackOverlayPresentation {
  private final ProgramController controller;
  private final PlaybackOverlayPane view;

  @Inject
  public PlaybackOverlayPresentation(final ProgramController controller, final PlaybackOverlayPane view) {
    this.controller = controller;
    this.view = view;
  }
}
