package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.PlaybackOverlayView;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.ListViewOption;
import hs.mediasystem.screens.optiondialog.NumericOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.SubOption;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.StringConverter;
import hs.subtitle.SubtitleDescriptor;

import java.util.ArrayList;
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

public class PlaybackOverlayPresentation {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final PlaybackOverlayView view;

  private final ObjectProperty<SubtitleDescriptor> selectedSubtitleForDownload = new SimpleObjectProperty<>();

  @Inject
  public PlaybackOverlayPresentation(final ProgramController controller, final PlayerPresentation playerPresentation, final PlaybackOverlayView view) {
    this.view = view;

    final Player player = playerPresentation.getPlayer();
    final MediaItem mediaItem = controller.getCurrentMediaItem();
    final PlayerBindings playerBindings = new PlayerBindings(view.playerProperty());

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
            new SubOption("Download subtitle...", new Callable<List<Option>>() {
              @Override
              public List<Option> call() {
                return new ArrayList<Option>() {{
                  final SubtitleSelector subtitleSelector = new SubtitleSelector(controller.getSubtitleProviders());

                  subtitleSelector.query(mediaItem);

                  subtitleSelector.subtitleProviderProperty().addListener(new ChangeListener<SubtitleProvider>() {
                    @Override
                    public void changed(ObservableValue<? extends SubtitleProvider> observableValue, SubtitleProvider oldValue, SubtitleProvider newValue) {
                      subtitleSelector.query(mediaItem);
                    }
                  });

                  add(new ListOption<>("Subtitle Provider", subtitleSelector.subtitleProviderProperty(), FXCollections.observableList(subtitleSelector.getSubtitleProviders()), new StringBinding(subtitleSelector.subtitleProviderProperty()) {
                    @Override
                    protected String computeValue() {
                      return subtitleSelector.subtitleProviderProperty().get().getName();
                    }
                  }));
                  add(new ListViewOption<>("Subtitles for Download", selectedSubtitleForDownload, subtitleSelector.getSubtitles(), new StringConverter<SubtitleDescriptor>() {
                    @Override
                    public String toString(SubtitleDescriptor object) {
                      return object.getName() + " (" + object.getLanguageName() + ") [" + object.getType() + "]";
                    }
                  }));
                }};
              }
            })
          );

          controller.showOptionScreen("Video - Options", options);
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
  }

  public Node getView() {
    return (Node)view;
  }
}
