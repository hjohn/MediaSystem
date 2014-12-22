package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.actions.Expose;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.screens.MainLocationPresentation;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.util.DialogPane;
import hs.mediasystem.util.javafx.Dialogs;

import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;

import javax.inject.Inject;

public class PlaybackOverlayPresentation extends MainLocationPresentation<PlaybackLocation> {

  public final ObjectProperty<Media> media = new SimpleObjectProperty<>();
  public final ObjectProperty<Player> player = new SimpleObjectProperty<>();
  @Expose
  public final BooleanProperty overlayVisible = new SimpleBooleanProperty(true);

  // TODO this binding is ugly, but it prevents a permanent reference to Player...
  private final LongBinding position = Bindings.selectLong(player, "position");

  private final Set<SubtitleProvider> subtitleProviders;
  private final Set<SubtitleCriteriaProvider> subtitleCriteriaProviders;
  private final ProgramController controller;

  @Inject
  public PlaybackOverlayPresentation(Set<SubtitleProvider> subtitleProviders, Set<SubtitleCriteriaProvider> subtitleCriteriaProviders, final ProgramController controller, final PlayerPresentation playerPresentation) {
    this.controller = controller;
    this.player.set(playerPresentation.getPlayer());
    this.media.set(controller.getCurrentMedia());

    this.subtitleProviders = subtitleProviders;
    this.subtitleCriteriaProviders = subtitleCriteriaProviders;

    position.addListener(new ChangeListener<Number>() {
      private long totalTimeViewed;
      private long timeViewedSinceLastSkip;

      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number value) {
        long old = oldValue.longValue();
        long current = value.longValue();

        if(old < current) {
          long diff = current - old;

          if(diff > 0 && diff < 4000) {
            totalTimeViewed += diff;
            timeViewedSinceLastSkip += diff;

            updatePositionAndViewed();
          }

          if(Math.abs(diff) >= 4000) {
            timeViewedSinceLastSkip = 0;
          }
        }
      }

      private void updatePositionAndViewed() {
        Player player = PlaybackOverlayPresentation.this.player.get();
        MediaData mediaData = media.get().getMediaItem().mediaData.get();

        if(mediaData != null) {
          long length = player.getLength();

          if(length > 0) {
            // TODO PlaybackLocation could be used to facilite skipping, not just the initial start position.  So skipping etc can be accomplished by location; however careful that for the same media initial resume position must be preserved for proper "viewed" calculation.
            long timeViewed = totalTimeViewed + location.get().getStartMillis();

            if(!mediaData.viewed.get() && timeViewed >= length * 9 / 10) {  // 90% viewed?
              System.out.println("[CONFIG] PlaybackOverlayPresentation - Marking as viewed: " + media.get());

              mediaData.viewed.set(true);
            }

            if(timeViewedSinceLastSkip > 30 * 1000) {
              int resumePosition = 0;
              long position = player.getPosition();

              if(position > 30 * 1000 && position < length * 9 / 10) {
                resumePosition = (int)(position / 1000) - 10;
              }

              if(Math.abs(mediaData.resumePosition.get() - resumePosition) > 10) {
                mediaData.resumePosition.set(resumePosition);
              }
            }
          }
        }
      }
    });
  }

  @Override
  public void dispose() {
    super.dispose();

    player.set(null);
  }

  @Expose
  public void chooseSubtitle(Event event) {
    Dialogs.show(event, new DialogPane<Void>() {{
      getChildren().add(new SubtitleDownloadPane(media.get(), subtitleProviders, subtitleCriteriaProviders, controller.getSubtitleDownloadService()));
    }});
    event.consume();
  }
}
