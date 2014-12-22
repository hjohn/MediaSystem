package hs.mediasystem.screens.playback;

import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.Location;
import javafx.scene.Node;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class PlaybackLayout implements Layout<Location, PlaybackOverlayPresentation> {
  private final Provider<PlaybackOverlayPresentation> presentationProvider;

  @Inject
  public PlaybackLayout(Provider<PlaybackOverlayPresentation> presentationProvider) {
    this.presentationProvider = presentationProvider;
  }

  @Override
  public Class<?> getContentClass() {
    return PlaybackLocation.class;
  }

  @Override
  public PlaybackOverlayPresentation createPresentation() {
    return presentationProvider.get();
  }

  @Override
  public Node createView(PlaybackOverlayPresentation presentation) {
    PlaybackOverlayPane view = new PlaybackOverlayPane();

    view.media.bindBidirectional(presentation.media);
    view.player.bindBidirectional(presentation.player);
    view.overlayVisible.bindBidirectional(presentation.overlayVisible);

    return view;
  }
}
