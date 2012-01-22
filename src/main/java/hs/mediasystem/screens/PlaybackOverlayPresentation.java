package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.StringConverter;
import hs.sublight.SubtitleDescriptor;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import javax.inject.Inject;

public class PlaybackOverlayPresentation {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final PlaybackOverlayPane view;

  private final ObjectProperty<SubtitleDescriptor> selectedSubtitleForDownload = new SimpleObjectProperty<>();

  @Inject
  public PlaybackOverlayPresentation(final ProgramController controller, final PlaybackOverlayPane view) {
    this.view = view;

    final Player player = controller.getPlayer();
    final MediaItem mediaItem = controller.getCurrentMediaItem();

    view.setTitle(mediaItem.getTitle());
    view.setSubtitle(mediaItem.getSubtitle());
    view.setReleaseYear("" + mediaItem.getReleaseYear());
    if(mediaItem.getPoster() != null) {
      view.setPoster(ImageCache.loadImage(mediaItem.getPoster())); // TODO might not be loaded yet!
    }

    view.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        KeyCode code = event.getCode();

        if(code == KeyCode.S) {
          controller.stop();
        }
        else if(code == KeyCode.J) {
          Subtitle subtitle = controller.nextSubtitle();

          if(subtitle != null) {
            view.setOSD("Subtitle: " + subtitle.getDescription());
          }
        }
        else if(BACK_SPACE.match(event)) {
          if(view.getChildren().size() > 1) {
            view.getChildren().remove(1);
            view.requestFocus();
          }
        }
        else if(code == KeyCode.O) {
          if(view.getChildren().size() == 1) {
            List<Option> options = FXCollections.observableArrayList(
              new NumericOption(player.volumeProperty(), "Volume", "%3.0f%%", 1, 0, 100),
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
              new NumericOption(player.rateProperty(), "Playback Speed", "%4.1f", 0.1, 0.1, 4.0),
              new NumericOption(player.audioDelayProperty(), "Audio Delay", "%5.0fms", 100, -30000, 30000),
              new NumericOption(player.brightnessProperty(), "Brightness", "%4.1f", 0.1, 0, 2),
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

            DialogScreen dialogScreen = new DialogScreen("Video - Options", options);

            view.getChildren().add(dialogScreen);

            dialogScreen.requestFocus();
          }
        }
        else if(code == KeyCode.SPACE) {
          controller.pause();
          view.showOSD();
        }
        else if(code == KeyCode.NUMPAD4) {
          controller.move(-10 * 1000);
          view.showOSD();
        }
        else if(code == KeyCode.NUMPAD6) {
          controller.move(10 * 1000);
          view.showOSD();
        }
        else if(code == KeyCode.NUMPAD2) {
          controller.move(-60 * 1000);
          view.showOSD();
        }
        else if(code == KeyCode.NUMPAD8) {
          controller.move(60 * 1000);
          view.showOSD();
        }
        else if(code == KeyCode.M) {
          controller.mute();
          view.showOSD();
        }
        else if(code == KeyCode.DIGIT9) {
          controller.changeVolume(-1);
          view.showOSD();
        }
        else if(code == KeyCode.DIGIT0) {
          controller.changeVolume(1);
          view.showOSD();
        }
        else if(code == KeyCode.DIGIT1) {
          controller.changeBrightness(-0.05f);
        }
        else if(code == KeyCode.DIGIT2) {
          controller.changeBrightness(0.05f);
        }
        else if(code == KeyCode.Z) {
          controller.changeSubtitleDelay(-100);
        }
        else if(code == KeyCode.X) {
          controller.changeSubtitleDelay(100);
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

    Timeline positionUpdater = new Timeline(
      new KeyFrame(Duration.seconds(0.10), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          view.setVolume(controller.getVolume() / 100.0);
          view.setPosition(controller.getPosition());

          long len = controller.getLength();

          if(len == 0) {
            len = 1;
          }

          view.setLength(len);
        }
      })
    );

    positionUpdater.setCycleCount(Animation.INDEFINITE);
    positionUpdater.play();
  }

  public PlaybackOverlayPane getView() {
    return view;
  }
}
