package hs.mediasystem.screens;

import hs.mediasystem.util.SizeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class PlaybackOverlayPane extends StackPane {
  private final StringProperty osdLine = new SimpleStringProperty("");

  private final BorderPane borderPane = new BorderPane();
  private final BorderPane bottomPane = new BorderPane();
  private final Label topLabel = new Label();

  private final Timeline osdFade = new Timeline(
    new KeyFrame(Duration.seconds(1), new KeyValue(topLabel.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(6), new KeyValue(topLabel.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(9), new KeyValue(topLabel.opacityProperty(), 0.0))
  );

  private final Timeline fadeInSustainAndFadeOut = new Timeline(
    new KeyFrame(Duration.seconds(0)),
    new KeyFrame(Duration.seconds(1), new KeyValue(bottomPane.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(6), new KeyValue(bottomPane.opacityProperty(), 1.0)),
    new KeyFrame(Duration.seconds(9), new KeyValue(bottomPane.opacityProperty(), 0.0))
  );

  public PlaybackOverlayPane() {
    final double w = 1920;  // TODO remove hardcoded values
    final double h = 1200;

    borderPane.setFocusTraversable(true);
    borderPane.setCenter(new VBox() {{
      getChildren().add(topLabel);
    }});

    topLabel.setId("video-osd-line");
    topLabel.textProperty().bind(osdLine);

    borderPane.setBottom(bottomPane);

    bottomPane.setId("video-overlay");
    bottomPane.setLeft(new ImageView() {{
      imageProperty().bind(poster);
      getStyleClass().add("poster");
      setFitWidth(w * 0.2);
      setFitHeight(h * 0.4);
      setPreserveRatio(true);
      setEffect(new Blend() {{
        setBottomInput(new DropShadow());
        setTopInput(new Reflection() {{
          this.setFraction(0.10);
        }});
      }});
    }});
    bottomPane.setCenter(new BorderPane() {{
      setId("video-overlay_info");
      setBottom(new HBox() {{
        getChildren().add(new VBox() {{
          HBox.setHgrow(this, Priority.ALWAYS);
          getChildren().add(new Label() {{
            textProperty().bind(title);
            getStyleClass().add("video-title");
            setEffect(createNeonEffect(64));
          }});
          getChildren().add(new Label() {{
            textProperty().bind(subtitle);
            getStyleClass().add("video-subtitle");
          }});
          getChildren().add(new GridPane() {{
//              setSpacing(20);
            setHgap(20);
            getColumnConstraints().addAll(
              new ColumnConstraints() {{
              }},
              new ColumnConstraints() {{
                setPercentWidth(60.0);   // this is not working as I want it.
                setHalignment(HPos.RIGHT);
              }},
              new ColumnConstraints() {{
                setPercentWidth(20.0);
              }},
              new ColumnConstraints() {{
              }}
            );
            setId("video-overlay_info_bar");
            add(new Label() {{
              textProperty().bind(new StringBinding() {
                {
                  bind(position);
                }

                @Override
                protected String computeValue() {
                  return SizeFormatter.SECONDS_AS_POSITION.format(position.get() / 1000);
                }
              });
            }}, 0, 0);
            add(new ProgressBar(0) {{
              getStyleClass().add("orange-bar");
              progressProperty().bind(Bindings.divide(Bindings.add(position, 0.0), length));
              setMaxWidth(100000);
              HBox.setHgrow(this, Priority.ALWAYS);
            }}, 1, 0, 2, 1);
            add(new Label() {{
              textProperty().bind(new StringBinding() {
                {
                  bind(length);
                }

                @Override
                protected String computeValue() {
                  return SizeFormatter.SECONDS_AS_POSITION.format(length.get() / 1000);
                }
              });
            }}, 3, 0);
            add(new Label("-"), 1, 1);
            add(new ProgressBar(0) {{
              getStyleClass().add("red-bar");
              progressProperty().bind(volume);
              setMaxWidth(100000);
              HBox.setHgrow(this, Priority.ALWAYS);
            }}, 2, 1);
            add(new Label("+"), 3, 1);
          }});
        }});
      }});
    }});

    getChildren().add(borderPane);

    sceneProperty().addListener(new ChangeListener<Scene>() {
      @Override
      public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
        if(newValue != null) {
          fadeInSustainAndFadeOut.playFromStart();
        }
      }
    });
  }

  public void showOSD() {
    fadeInSustainAndFadeOut.playFromStart();
  }

  public void setOSD(String text) {
    osdLine.set(text);
    osdFade.playFromStart();
  }

  private static Effect createNeonEffect(final double size) { // font point size
    return new Blend() {{
      setMode(BlendMode.MULTIPLY);
      setBottomInput(new DropShadow() {{
        setColor(Color.rgb(254, 235, 66, 0.3));
        setOffsetX(size / 22);
        setOffsetY(size / 22);
        setSpread(0.2);
      }});
      setTopInput(new Blend() {{
        setMode(BlendMode.MULTIPLY);
        setBottomInput(new DropShadow() {{
          setColor(Color.web("#f13a00"));
          setRadius(size / 5.5);
          setSpread(0.2);
        }});
        setTopInput(new Blend() {{
          setMode(BlendMode.MULTIPLY);
          setBottomInput(new InnerShadow() {{
            setColor(Color.web("#feeb42"));
            setRadius(size / 12);
            setChoke(0.8);
          }});
          setTopInput(new InnerShadow() {{
            setColor(Color.web("#f13a00"));
            setRadius(size / 22);
            setChoke(0.4);
          }});
        }});
      }});
    }};
  }

  private final DoubleProperty volume = new SimpleDoubleProperty(0);
  public double getVolume() { return volume.get(); }
  public void setVolume(double volume) { this.volume.set(volume); }
  public DoubleProperty volumeProperty() { return volume; }

  private final LongProperty position = new SimpleLongProperty();
  public long getPosition() { return position.get(); }
  public void setPosition(long position) { this.position.set(position); }
  public LongProperty positionProperty() { return position; }

  private final LongProperty length = new SimpleLongProperty(1);
  public long getLength() { return length.get(); }
  public void setLength(long length) { this.length.set(length); }
  public LongProperty lengthProperty() { return length; }

  private final StringProperty title = new SimpleStringProperty();
  public String getTitle() { return title.get(); }
  public void setTitle(String title) { this.title.set(title); }
  public StringProperty titleProperty() { return title; }

  private final StringProperty subtitle = new SimpleStringProperty();
  public String getSubtitle() { return subtitle.get(); }
  public void setSubtitle(String subtitle) { this.subtitle.set(subtitle); }
  public StringProperty subtitleProperty() { return subtitle; }

  private final StringProperty releaseYear = new SimpleStringProperty();
  public String getReleaseYear() { return releaseYear.get(); }
  public void setReleaseYear(String releaseYear) { this.releaseYear.set(releaseYear); }
  public StringProperty releaseYearProperty() { return releaseYear; }

  private final ObjectProperty<Image> poster = new SimpleObjectProperty<>();
  public Image getPoster() { return poster.get(); }
  public void setPoster(Image poster) { this.poster.set(poster); }
  public ObjectProperty<Image> posterProperty() { return poster; }

}
