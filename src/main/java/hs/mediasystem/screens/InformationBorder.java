package hs.mediasystem.screens;

import hs.mediasystem.util.SizeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class InformationBorder extends HBox {
  private final Label clock = new Label() {{
    getStyleClass().add("clock");
  }};

  private final Rectangle memoryBar = new Rectangle();

  private final Label memText = new Label() {{
    setBlendMode(BlendMode.DIFFERENCE);
  }};

  private final StackPane gc = new StackPane() {{
    getStyleClass().add("memory");
    getChildren().add(memoryBar);
    getChildren().add(memText);
  }};

  public InformationBorder() {
    getChildren().addAll(clock, gc);

    Timeline updater = new Timeline(
      new KeyFrame(Duration.seconds(0.10), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          clock.setText(String.format("%1$tA, %1$te %1$tB %1$tY %1$tT", System.currentTimeMillis()));
          Runtime runtime = Runtime.getRuntime();

          long totalMemory = runtime.totalMemory();
          long freeMemory = runtime.freeMemory();
          long maxMemory = runtime.maxMemory();

          double percentageUsed = (double)(totalMemory - freeMemory) / maxMemory;
          double percentageFree = percentageUsed + (double)freeMemory / maxMemory;

          memoryBar.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.YELLOW),
            new Stop(percentageUsed, Color.YELLOW),
            new Stop(percentageUsed + 0.0001, Color.LIGHTGRAY),
            new Stop(percentageFree, Color.LIGHTGRAY),
            new Stop(percentageFree + 0.0001, Color.TRANSPARENT),
            new Stop(1, Color.TRANSPARENT)
          ));

          memoryBar.setWidth(300);
          memoryBar.setHeight(memText.getHeight());

          memText.setText(SizeFormatter.BYTES_THREE_SIGNIFICANT.format(runtime.totalMemory()) + "/" + SizeFormatter.BYTES_THREE_SIGNIFICANT.format(runtime.maxMemory()));
        }
      })
    );

    updater.setCycleCount(Timeline.INDEFINITE);
    updater.play();
  }
}
