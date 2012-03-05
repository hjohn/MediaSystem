package hs.mediasystem.screens;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.player.AudioTrack;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.Subtitle;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.ListViewOption;
import hs.mediasystem.screens.optiondialog.NumericOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.SubOption;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.SizeFormatter;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.StringConverter;
import hs.sublight.SubtitleDescriptor;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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
            new NumericOption(player.audioDelayProperty(), "Audio Delay", 100, -1200000, 1200000, "%5.0fms"),
            new NumericOption(player.subtitleDelayProperty(), "Subtitle Delay", 100, -1200000, 1200000, "%5.0fms"),
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
          view.addOSD(createOSDItem("Position", 0.0, 100.0, player.positionProperty().multiply(100.0).divide(player.lengthProperty()), new StringBinding(player.positionProperty()) {
            @Override
            protected String computeValue() {
              return SizeFormatter.SECONDS_AS_POSITION.format(player.getPosition() / 1000);
            }
          }));
        }
      }
    });

    player.volumeProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        view.addOSD(createOSDItem("Volume", 0.0, 100.0, player.volumeProperty(), new StringBinding(player.volumeProperty()) {
          @Override
          protected String computeValue() {
            return String.format("%3d%%", player.volumeProperty().get());
          }
        }));
      }
    });

    player.rateProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        view.addOSD(createOSDItem("Playback Speed", 0.0, 4.0, player.rateProperty(), new StringBinding(player.rateProperty()) {
          @Override
          protected String computeValue() {
            return String.format("%4.1f speed", player.rateProperty().get());
          }
        }));
      }
    });

    player.audioDelayProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        view.addOSD(createOSDItem("Audio Delay", -120.0, 120.0, player.audioDelayProperty().divide(1000.0), new StringBinding(player.audioDelayProperty()) {
          @Override
          protected String computeValue() {
            return String.format("%5.1fs", player.audioDelayProperty().get() / 1000.0);
          }
        }));
      }
    });

    player.subtitleDelayProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        view.addOSD(createOSDItem("Subtitle Delay", -120.0, 120.0, player.subtitleDelayProperty().divide(1000.0), new StringBinding(player.subtitleDelayProperty()) {
          @Override
          protected String computeValue() {
            return String.format("%5.1fs", player.subtitleDelayProperty().get() / 1000.0);
          }
        }));
      }
    });

    player.brightnessProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        view.addOSD(createOSDItem("Brightness", -100.0, 100.0, player.brightnessProperty().subtract(1.0).multiply(100.0), new StringBinding(player.brightnessProperty()) {
          @Override
          protected String computeValue() {
            return String.format("%+3d%%", Math.round((player.brightnessProperty().get() - 1.0) * 100.0));
          }
        }));
      }
    });
  }

  public PlaybackOverlayPane getView() {
    return view;
  }

  private Node createOSDItem(final String title, final double min, final double max, final NumberExpression value, final StringExpression valueText) {
    return new VBox() {{
      setId(title);
      getStyleClass().add("item");
      getChildren().add(new BorderPane() {{
        setLeft(new Label(title) {{
          getStyleClass().add("title");
        }});
        setCenter(new Label() {{
          getStyleClass().add("value");
          textProperty().bind(valueText);
        }});
      }});
      getChildren().add(new Slider(min, max * 1.01, 0) {{  // WORKAROUND: 1.01 to work around last label display bug
        valueProperty().bind(value);
        setMinorTickCount(4);
        setMajorTickUnit(max / 4);
      }});
    }};
  }
}
