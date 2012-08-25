package hs.mediasystem.screens;

import hs.mediasystem.dao.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.PlaybackOverlayView;
import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.ListViewOption;
import hs.mediasystem.screens.optiondialog.NumericOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.OptionDialogPane;
import hs.mediasystem.screens.optiondialog.OptionGroup;
import hs.mediasystem.util.PropertyClassEq;
import hs.mediasystem.util.PropertyEq;
import hs.mediasystem.util.ServiceTracker;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.StringConverter;
import hs.subtitle.SubtitleDescriptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javax.inject.Inject;
import javax.inject.Provider;

import org.osgi.framework.BundleContext;

public class PlaybackOverlayPresentation implements Presentation {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);
  private static final KeyCombination KEY_I = new KeyCodeCombination(KeyCode.I);

  private static final Comparator<SubtitleProvider> SUBTITLE_PROVIDER_COMPARATOR = new Comparator<SubtitleProvider>() {
    @Override
    public int compare(SubtitleProvider o1, SubtitleProvider o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  private final PlaybackOverlayView view;
  private final PlayerPresentation playerPresentation;
  private final MediaItem mediaItem;

  private final ObjectProperty<SubtitleDescriptor> selectedSubtitleForDownload = new SimpleObjectProperty<>();

  private long totalTimeViewed = 0;

  @Inject
  public PlaybackOverlayPresentation(BundleContext bundleContext, final ProgramController controller, final PlayerPresentation playerPresentation, final PlaybackOverlayView view) {
    this.playerPresentation = playerPresentation;
    this.view = view;
    this.mediaItem = controller.getCurrentMediaItem();

    final Player player = playerPresentation.getPlayer();
    final PlayerBindings playerBindings = new PlayerBindings(view.playerProperty());

    final ServiceTracker<SubtitleProvider> subtitleProviderTracker = new ServiceTracker<>(bundleContext, SubtitleProvider.class);
    final ServiceTracker<SubtitleCriteriaProvider> subtitleCriteriaProviderTracker = new ServiceTracker<>(bundleContext, SubtitleCriteriaProvider.class);

    view.mediaItemProperty().set(mediaItem);
    view.playerProperty().set(player);

    getView().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        KeyCode code = event.getCode();

        if(code == KeyCode.J) {
          playerPresentation.nextSubtitle();
          event.consume();
        }
        else if(KEY_I.match(event)) {
          view.toggleVisibility();
        }
        else if(BACK_SPACE.match(event)) {
          event.consume();
        }
        else if(code == KeyCode.O) {
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
                  final SubtitleSelector subtitleSelector = new SubtitleSelector(subtitleProviderTracker.getServices(SUBTITLE_PROVIDER_COMPARATOR, new PropertyEq("mediatype", "movie")));
                  final SubtitleCriteriaProvider subtitleCriteriaProvider = subtitleCriteriaProviderTracker.getService(new PropertyClassEq("mediasystem.class", mediaItem.getMedia().getClass()));

                  subtitleSelector.query(subtitleCriteriaProvider.getCriteria(mediaItem));

                  subtitleSelector.subtitleProviderProperty().addListener(new ChangeListener<SubtitleProvider>() {
                    @Override
                    public void changed(ObservableValue<? extends SubtitleProvider> observableValue, SubtitleProvider oldValue, SubtitleProvider newValue) {
                      subtitleSelector.query(subtitleCriteriaProvider.getCriteria(mediaItem));
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

          controller.showDialog(new OptionDialogPane("Video - Options", options));
          event.consume();
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

    playerPresentation.getPlayer().positionProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number value) {
        long old = oldValue.longValue();
        long current = value.longValue();

        if(old < current) {
          long diff = current - old;

          if(diff < 1000) {
            totalTimeViewed += diff;
          }
        }
      }
    });
  }

  @Override
  public Node getView() {
    return (Node)view;
  }

  @Override
  public void dispose() {
    MediaData mediaData = mediaItem.get(MediaData.class);

    if(mediaData != null) {
      long length = playerPresentation.getLength();

      if(length > 0) {
        long timeViewed = totalTimeViewed + mediaData.getResumePosition() * 1000L;

        if(timeViewed >= length * 9 / 10) {  // 90% viewed?
          System.out.println("[CONFIG] ProgramController.play(...).new Destination() {...}.outro() - Marking as viewed: " + mediaItem);

          mediaData.viewedProperty().set(true);
        }

        if(totalTimeViewed > 30 * 1000) {
          long resumePosition = 0;
          long position = playerPresentation.getPosition();

          if(position > 30 * 1000 && position < length * 9 / 10) {
            System.out.println("[CONFIG] ProgramController.play(...).new Destination() {...}.outro() - Setting resume position to " + position + " ms: " + mediaItem);

            resumePosition = position;
          }

          mediaData.resumePositionProperty().set((int)(resumePosition / 1000));
        }
      }
    }
  }
}
