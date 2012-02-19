package hs.mediasystem.screens;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.StringConverter;
import hs.sublight.SubtitleDescriptor;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javax.inject.Inject;

public class PlaybackOverlayPresentation {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final PlaybackOverlayPane view;

  private final ObjectProperty<SubtitleDescriptor> selectedSubtitleForDownload = new SimpleObjectProperty<>();

  @Inject
  public PlaybackOverlayPresentation(final ProgramController controller, final PlayerPresentation playerPresentation, final PlaybackOverlayPane view) {
    this.view = view;

    final Player player = playerPresentation.getPlayer();
    final MediaItem mediaItem = controller.getCurrentMediaItem();

    if(mediaItem.getMediaType().equals("EPISODE")) {
      view.titleProperty().bind(mediaItem.groupNameProperty());
      view.subtitleProperty().bind(mediaItem.titleProperty());
    }
    else {
      view.titleProperty().bind(mediaItem.titleProperty());
      view.subtitleProperty().bind(mediaItem.subtitleProperty());
    }
    view.releaseYearProperty().bind(Bindings.convert(mediaItem.releaseYearProperty()));
    view.posterProperty().bind(new AsyncImageProperty(mediaItem.posterProperty()));

    view.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        KeyCode code = event.getCode();

        if(code == KeyCode.J) {
          Subtitle subtitle = playerPresentation.nextSubtitle();

          if(subtitle != null) {
            view.setOSD("Subtitle: " + subtitle.getDescription());
          }
          event.consume();
        }
        else if(BACK_SPACE.match(event)) {
          event.consume();
        }
        else if(code == KeyCode.O) {
          List<Option> options = FXCollections.observableArrayList(
            new NumericOption(player.volumeProperty(), "Volume", 1, 0, 100, "%3.0f%%"),
            new ListOption<>("Subtitle", player.subtitleProperty(), player.getSubtitles(), new StringConverter<Subtitle>() {
              @Override
              public String toString(Subtitle object) {
                return object.getDescription();
              }
            }),
            new ListOption<>("Audio Track", player.audioTrackProperty(), player.getAudioTracks(), new StringConverter<AudioTrack>() {
              @Override
              public String toString(AudioTrack object) {
                return object.getDescription();
              }
            }),
            new NumericOption(player.rateProperty(), "Playback Speed", 0.1, 0.1, 4.0, "%4.1f"),
            new NumericOption(player.audioDelayProperty(), "Audio Delay", 100, -30000, 30000, "%5.0fms"),
            new NumericOption(player.brightnessProperty(), "Brightness adjustment", 0.01, 0, 2, new StringConverter<Number>() {
              @Override
              public String toString(Number object) {
                long value = Math.round((object.doubleValue() - 1.0) * 100);
                return value == 0 ? "0%" : String.format("%+3d%%", value);
              }
            }),
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

                  add(new ListOption<>("Subtitle Provider", subtitleSelector.subtitleProviderProperty(), FXCollections.observableList(subtitleSelector.getSubtitleProviders()), new StringConverter<SubtitleProvider>() {
                    @Override
                    public String toString(SubtitleProvider object) {
                      return object.getName();
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

    view.positionProperty().bind(player.positionProperty());
    view.lengthProperty().bind(player.lengthProperty());

    player.positionProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if(Math.abs(oldValue.longValue() - newValue.longValue()) > 2500) {
          view.showOSD();
        }
      }
    });
  }

  public PlaybackOverlayPane getView() {
    return view;
  }
}
