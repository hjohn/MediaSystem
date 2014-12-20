package hs.mediasystem.screens.collection;

import java.io.ByteArrayInputStream;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class BackgroundPane extends StackPane {
  private static final Duration SETTLE_DURATION = Duration.millis(1000);  // TODO this helps "prevent" null images being shown between series and serie switches, but not always... should be resolved on a series/serie level instead
  private static final Image EMPTY_IMAGE = new Image(new ByteArrayInputStream(new byte[0]));

  private final ObjectProperty<MediaNode> mediaNode = new SimpleObjectProperty<>();
  public ObjectProperty<MediaNode> mediaNodeProperty() { return mediaNode; }

  private final ObjectBinding<ImageHandle> backgroundHandle = MapBindings.select(mediaNode, "media", "background");

  private final AsyncImageProperty wantedBackground = new AsyncImageProperty();

  private final ObjectProperty<Image> background = new SimpleObjectProperty<>(EMPTY_IMAGE);
  private final ObjectProperty<Image> newBackground = new SimpleObjectProperty<>(EMPTY_IMAGE);

  private final ScaledImageView backgroundImageView = new ScaledImageView() {{
    imageProperty().bind(background);
    setPreserveRatio(true);
    setSmooth(true);
    setAlignment(Pos.TOP_CENTER);
    setZoom(true);
  }};

  private final ScaledImageView newBackgroundImageView = new ScaledImageView() {{
    imageProperty().bind(newBackground);
    setPreserveRatio(true);
    setSmooth(true);
    setAlignment(Pos.TOP_CENTER);
    setZoom(true);
  }};

  private final EventHandler<ActionEvent> beforeBackgroundChange = new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent event) {

      /*
       * Check if background really has changed during the SETTLE_DURATION, if not, cancel the animation.
       */

      if(isBackgroundChanged()) {
        Image image = wantedBackground.get();

        if(image == null) {
          image = EMPTY_IMAGE;  // WORKAROUND for ImageViews being unable to handle null properly
        }

        newBackground.set(image);
      }
      else {
        timeline.stop();
      }
    }
  };

  private final Timeline timeline = new Timeline(
    new KeyFrame(Duration.ZERO,
      new KeyValue(backgroundImageView.opacityProperty(), 1.0),
      new KeyValue(newBackgroundImageView.opacityProperty(), 0.0)
    ),
    new KeyFrame(SETTLE_DURATION, beforeBackgroundChange,
      new KeyValue(backgroundImageView.opacityProperty(), 1.0),
      new KeyValue(newBackgroundImageView.opacityProperty(), 0.0)
    ),
    new KeyFrame(Duration.millis(4000),
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

        if(isBackgroundChanged()) {
          timeline.play();
        }
      }
    });

    wantedBackground.imageHandleProperty().bind(backgroundHandle);

    wantedBackground.addListener(new ChangeListener<Image>() {
      @Override
      public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue) {

        /*
         * If the background should change and we are not currently in the change process, then start a new animation.  If we are
         * in the early stages of an animation, restart it to allow the background to 'settle'.
         */

        if(timeline.getStatus() == Animation.Status.STOPPED || timeline.getCurrentTime().lessThan(SETTLE_DURATION)) {
          timeline.playFromStart();
        }
      }
    });

    getChildren().addAll(backgroundImageView, newBackgroundImageView);
  }

  private boolean isBackgroundChanged() {
    return (wantedBackground.get() == null && background.get() != null) || (wantedBackground.get() != null && !wantedBackground.get().equals(background.get()));
  }
}
