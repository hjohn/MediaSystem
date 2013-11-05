package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.screens.MainLocationPresentation;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.ListViewOption;
import hs.mediasystem.screens.optiondialog.NumericOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.OptionDialogPane;
import hs.mediasystem.screens.optiondialog.OptionGroup;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.StringConverter;
import hs.subtitle.SubtitleDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.LongBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

import javax.inject.Inject;
import javax.inject.Provider;

public class PlaybackOverlayPresentation extends MainLocationPresentation {
  private static final Comparator<SubtitleProvider> SUBTITLE_PROVIDER_COMPARATOR = new Comparator<SubtitleProvider>() {
    @Override
    public int compare(SubtitleProvider o1, SubtitleProvider o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  public final ObjectProperty<MediaItem> mediaItem = new SimpleObjectProperty<>();
  public final ObjectProperty<Player> player = new SimpleObjectProperty<>();
  public final BooleanProperty overlayVisible = new SimpleBooleanProperty(true);

  // TODO this binding is ugly, but it prevents a permanent reference to Player...
  private final LongBinding position = Bindings.selectLong(player, "position");

  private final ObjectProperty<SubtitleDescriptor> selectedSubtitleForDownload = new SimpleObjectProperty<>();
  private final Set<SubtitleProvider> subtitleProviders;
  private final Set<SubtitleCriteriaProvider> subtitleCriteriaProviders;
  private final PlayerBindings playerBindings = new PlayerBindings(player);

  @Inject
  public PlaybackOverlayPresentation(Set<SubtitleProvider> subtitleProviders, Set<SubtitleCriteriaProvider> subtitleCriteriaProviders, final ProgramController controller, final PlayerPresentation playerPresentation) {
    super(controller);

    this.player.set(playerPresentation.getPlayer());
    this.mediaItem.set(controller.getCurrentMediaItem());

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
        MediaData mediaData = mediaItem.get().mediaData.get();

        if(mediaData != null) {
          long length = player.getLength();

          if(length > 0) {
            // TODO PlaybackLocation could be used to facilite skipping, not just the initial start position.  So skipping etc can be accomplished by location; however careful that for the same media initial resume position must be preserved for proper "viewed" calculation.
            long timeViewed = totalTimeViewed + ((PlaybackLocation)location.get()).getStartMillis();

            if(!mediaData.viewed.get() && timeViewed >= length * 9 / 10) {  // 90% viewed?
              System.out.println("[CONFIG] PlaybackOverlayPresentation - Marking as viewed: " + mediaItem.get());

              mediaData.viewed.set(true);
            }

            if(timeViewedSinceLastSkip > 30 * 1000) {
              int resumePosition = 0;
              long position = player.getPosition();

              if(position > 30 * 1000 && position < length * 9 / 10) {
                resumePosition = (int)(position / 1000) - 10;
              }

              if(Math.abs(mediaData.resumePosition.get() - resumePosition) > 10) {
                System.out.println("[CONFIG] PlaybackOverlayPresentation - Setting resume position to " + position + " ms: " + mediaItem.get());

                mediaData.resumePosition.set(resumePosition);
              }
            }
          }
        }
      }
    });

    selectedSubtitleForDownload.addListener(new ChangeListener<SubtitleDescriptor>() {
      @Override
      public void changed(ObservableValue<? extends SubtitleDescriptor> observable, SubtitleDescriptor oldValue, SubtitleDescriptor newValue) {
        if(newValue != null) {
          SubtitleDownloadService service = controller.getSubtitleDownloadService();

          service.setSubtitleDescriptor(newValue);
          service.restart();
        }
      }
    });
  }

  @Override
  protected void dispose() {
    player.set(null);
  }

  public void handleOptionsSelectEvent(ActionEvent event) {
    Player player = this.player.get();

    List<Option> options = FXCollections.observableArrayList(
      new NumericOption(player.volumeProperty(), "Volume", 1, 0, 100, playerBindings.formattedVolume),
      new ListOption<>("Subtitle", player.subtitleProperty(), player.getSubtitles(), playerBindings.formattedSubtitle),
      new ListOption<>("Audio Track", player.audioTrackProperty(), player.getAudioTracks(), playerBindings.formattedAudioTrack),
      new NumericOption(player.rateProperty(), "Playback Speed", 0.1, 0.1, 4.0, playerBindings.formattedRate),
      new NumericOption(player.audioDelayProperty(), "Audio Delay", 100, -1200000, 1200000, playerBindings.formattedAudioDelay),
      new NumericOption(player.subtitleDelayProperty(), "Subtitle Delay", 100, -1200000, 1200000, playerBindings.formattedSubtitleDelay),
      new NumericOption(player.brightnessProperty(), "Brightness Adjustment", 0.01, 0, 2, playerBindings.formattedBrightness),
      new OptionGroup("Download subtitle...", new Provider<List<Option>>() {
        @Override
        public List<Option> get() {
          return new ArrayList<Option>() {{
            final SubtitleSelector subtitleSelector = new SubtitleSelector(findSubtitleProviders("movie"));
            final SubtitleCriteriaProvider subtitleCriteriaProvider = findSubtitleCriteriaProvider(mediaItem.get().getMedia().getClass());  // TODO NPE, getMedia() can be null when not identified (when downloading subs)

            subtitleSelector.query(subtitleCriteriaProvider.getCriteria(mediaItem.get()));

            subtitleSelector.subtitleProviderProperty().addListener(new ChangeListener<SubtitleProvider>() {
              @Override
              public void changed(ObservableValue<? extends SubtitleProvider> observableValue, SubtitleProvider oldValue, SubtitleProvider newValue) {
                subtitleSelector.query(subtitleCriteriaProvider.getCriteria(mediaItem.get()));
              }
            });

            ListOption<SubtitleProvider> provider = new ListOption<>("Subtitle Provider", subtitleSelector.subtitleProviderProperty(), FXCollections.observableList(subtitleSelector.getSubtitleProviders()), new StringBinding(subtitleSelector.subtitleProviderProperty()) {
              @Override
              protected String computeValue() {
                return subtitleSelector.subtitleProviderProperty().get().getName();
              }
            });

            provider.getBottomLabel().textProperty().bind(subtitleSelector.statusTextProperty());

            add(provider);
            add(new ListViewOption<>("Subtitles for Download", selectedSubtitleForDownload, subtitleSelector.getSubtitles(), new StringConverter<SubtitleDescriptor>() {
              @Override
              public String toString(SubtitleDescriptor descriptor) {
                return descriptor.getMatchType().name() + ": " + descriptor.getName() + " (" + descriptor.getLanguageName() + ") [" + descriptor.getType() + "]";
              }
            }));
          }};
        }
      })
    );

    getProgramController().showDialog(new OptionDialogPane("Video - Options", options));
    event.consume();
  }

  private List<SubtitleProvider> findSubtitleProviders(String mediaType) {
    List<SubtitleProvider> matchingSubtitleProviders = new ArrayList<>();

    for(SubtitleProvider provider : subtitleProviders) {
      if(provider.getMediaTypes().contains(mediaType)) {
        matchingSubtitleProviders.add(provider);
      }
    }

    Collections.sort(matchingSubtitleProviders, SUBTITLE_PROVIDER_COMPARATOR);

    return matchingSubtitleProviders;
  }

  private SubtitleCriteriaProvider findSubtitleCriteriaProvider(Class<?> cls) {
    for(SubtitleCriteriaProvider provider : subtitleCriteriaProviders) {
      if(provider.getMediaType().equals(cls)) {
        return provider;
      }
    }

    return null;
  }
}
