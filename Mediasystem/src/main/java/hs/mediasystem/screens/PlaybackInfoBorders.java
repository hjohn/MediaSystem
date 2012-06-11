package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.util.GridPaneUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class PlaybackInfoBorders extends StackPane {
  private final ObjectProperty<MediaItem> mediaItem = new SimpleObjectProperty<>();
  public ObjectProperty<MediaItem> mediaItemProperty() { return mediaItem; }

  private final ObjectProperty<Player> player = new SimpleObjectProperty<>();
  public ObjectProperty<Player> playerProperty() { return player; }

  private final PlayerBindings playerBindings = new PlayerBindings(player);
  private final StringProperty formattedTime = new SimpleStringProperty();

  public PlaybackInfoBorders() {
    getStylesheets().add("playback-info-borders.css");
    getStyleClass().add("grid");

    GridPane grid = GridPaneUtil.create(new double[] {33, 34, 33}, new double[] {50, 50});

    grid.add(new Label() {{
      GridPane.setHalignment(this, HPos.LEFT);
      GridPane.setValignment(this, VPos.BOTTOM);
      textProperty().bind(Bindings.concat(playerBindings.formattedPosition, "/", playerBindings.formattedLength));
    }}, 0, 1);

    grid.add(new Label() {{
      GridPane.setHalignment(this, HPos.RIGHT);
      GridPane.setValignment(this, VPos.BOTTOM);
      textProperty().bind(formattedTime);
    }}, 2, 1);

    Timeline updater = new Timeline(
      new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          formattedTime.set(String.format("%1$tR", System.currentTimeMillis()));
        }
      })
    );

    updater.setCycleCount(Animation.INDEFINITE);
    updater.play();

    getChildren().add(grid);
  }
}
