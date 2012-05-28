package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.media.Episode;
import hs.mediasystem.media.Media;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class BackgroundPane extends ScrollPane {
  private final ObjectProperty<MediaNode> mediaNode = new SimpleObjectProperty<>();
  public ObjectProperty<MediaNode> mediaNodeProperty() { return mediaNode; }

  private final ObjectBinding<ImageHandle> nodeBackgroundHandle = MapBindings.select(mediaNode, "dataMap", Media.class, "background");
  private final ObjectBinding<ImageHandle> parentBackgroundHandle = MapBindings.select(mediaNode, "dataMap", Episode.class, "serie", "dataMap", Media.class, "background");
  private final ObjectBinding<ImageHandle> backgroundHandle = Bindings.when(nodeBackgroundHandle.isNull()).then(parentBackgroundHandle).otherwise(nodeBackgroundHandle);

  private final AsyncImageProperty wantedBackground = new AsyncImageProperty();

  private final ObjectProperty<Image> background = new SimpleObjectProperty<>();
  private final ObjectProperty<Image> newBackground = new SimpleObjectProperty<>();

  private final ImageView backgroundImageView = new ImageView() {{
    imageProperty().bind(background);
    setPreserveRatio(true);
    setSmooth(true);
  }};

  private final ImageView newBackgroundImageView = new ImageView() {{
    imageProperty().bind(newBackground);
    setPreserveRatio(true);
    setSmooth(true);
  }};

  private final Timeline timeline = new Timeline(
    new KeyFrame(Duration.ZERO,
      new KeyValue(backgroundImageView.opacityProperty(), 1.0),
      new KeyValue(newBackgroundImageView.opacityProperty(), 0.0)
    ),
    new KeyFrame(new Duration(4000),
      new KeyValue(backgroundImageView.opacityProperty(), 0.0),
      new KeyValue(newBackgroundImageView.opacityProperty(), 1.0)
    )
  );

  public BackgroundPane() {
    timeline.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        background.set(newBackground.get());
        backgroundImageView.setOpacity(1.0);
        newBackgroundImageView.setOpacity(0.0);

        if((wantedBackground.get() == null && background.get() != null) || (wantedBackground.get() != null && !wantedBackground.get().equals(background.get()))) {
          newBackground.set(wantedBackground.get());
          timeline.play();
        }
      }
    });

    wantedBackground.imageHandleProperty().bind(backgroundHandle);

    wantedBackground.addListener(new ChangeListener<Image>() {
      @Override
      public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue) {
        if(timeline.getStatus() == Animation.Status.STOPPED) {
          newBackground.set(wantedBackground.get());
          timeline.play();
        }
      }
    });

    final ReadOnlyDoubleProperty widthProperty = widthProperty();
    final ReadOnlyDoubleProperty heightProperty = heightProperty();

    setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    setContent(new Group() {{
      getChildren().addAll(backgroundImageView, newBackgroundImageView);

      backgroundImageView.fitWidthProperty().bind(widthProperty);
      backgroundImageView.fitHeightProperty().bind(heightProperty);
      newBackgroundImageView.fitWidthProperty().bind(widthProperty);
      newBackgroundImageView.fitHeightProperty().bind(heightProperty);
    }});
  }
}
