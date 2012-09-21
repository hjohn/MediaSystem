package hs.mediasystem.screens;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.DatabaseException;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.util.SizeFormatter;
import hs.mediasystem.util.SpecialEffects;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
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

import javax.inject.Inject;

public class InformationBorder extends HBox {
  private final HBox programName = new HBox() {{
    getStyleClass().addAll("program-name", "element");
    getChildren().add(new Label("Media") {{
      getStyleClass().add("left");
    }});
    getChildren().add(new Label("S") {{
      getStyleClass().add("center");
    }});
    getChildren().add(new Label("ystem") {{
      getStyleClass().add("right");
    }});
    setEffect(SpecialEffects.createNeonEffect(12));
  }};

  private final Label clock = new Label() {{
    getStyleClass().add("clock");
  }};

  private final HBox clockElement = new HBox() {{
    getStyleClass().add("element");
    getChildren().add(clock);
  }};

  private final Label databaseSize = new Label() {{
    getStyleClass().add("database-size");
  }};

  private final HBox databaseSizeElement = new HBox() {{
    getStyleClass().add("element");
    getChildren().add(databaseSize);
  }};

  private final Label breadCrumb = new Label() {{
    getStyleClass().add("bread-crumb");
  }};

  private final HBox breadCrumbElement = new HBox() {{
    getStyleClass().add("element");
    getChildren().add(breadCrumb);
  }};

  private final Rectangle memoryBar = new Rectangle();
  private final Label memText = new Label();

  private final StackPane gc = new StackPane() {{
    getStyleClass().addAll("element", "memory");

    getChildren().add(new StackPane() {{
      getStyleClass().add("bar");
      getChildren().addAll(memoryBar, memText);
    }});

    memText.setBlendMode(BlendMode.DIFFERENCE);

    memoryBar.setWidth(150);
    memoryBar.heightProperty().bind(memText.heightProperty());
  }};

  @Inject
  public InformationBorder(final Database database, SettingsStore settingsStore) {
    getStylesheets().add("information-border.css");

    BooleanProperty informationBarActive = settingsStore.getBooleanProperty("MediaSystem:InformationBar", PersistLevel.PERMANENT, "Visible");

    gc.visibleProperty().bind(informationBarActive);
    gc.managedProperty().bind(informationBarActive);

    getChildren().add(new HBox() {{
      getStyleClass().add("elements");
      getChildren().addAll(programName, clockElement, databaseSizeElement, gc, breadCrumbElement);
    }});

    Timeline updater = new Timeline(
      new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
        private int databaseSizeUpdateCounter = 0;

        @Override
        public void handle(ActionEvent event) {
          clock.setText(String.format("%1$tA, %1$te %1$tB %1$tY  %1$tR", System.currentTimeMillis()));

          Runtime runtime = Runtime.getRuntime();

          long maxMemory = runtime.maxMemory();
          long totalMemory = runtime.totalMemory();
          long usedMemory = totalMemory - runtime.freeMemory();

          double percentageUsed = (double)usedMemory / maxMemory;
          double percentageTotal = (double)totalMemory / maxMemory;

          memoryBar.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.WHITE),
            new Stop(percentageUsed, Color.WHITE),
            new Stop(percentageUsed + 0.0001, Color.LIGHTGRAY),
            new Stop(percentageTotal, Color.LIGHTGRAY),
            new Stop(percentageTotal + 0.0001, Color.TRANSPARENT),
            new Stop(1, Color.TRANSPARENT)
          ));

          memText.setText(SizeFormatter.BYTES_THREE_SIGNIFICANT.format(usedMemory) + "/" + SizeFormatter.BYTES_THREE_SIGNIFICANT.format(totalMemory));

          if(databaseSizeUpdateCounter-- == 0) {
            try(Transaction transaction = database.beginReadOnlyTransaction()) {
              databaseSize.setText(SizeFormatter.BYTES_THREE_SIGNIFICANT.format(transaction.getDatabaseSize()));
              databaseSizeUpdateCounter = 60;
            }
            catch(DatabaseException e) {
              databaseSize.setText("-");
              databaseSizeUpdateCounter = 600;
            }
          }
        }
      })
    );

    updater.setCycleCount(Animation.INDEFINITE);
    updater.play();
  }

  public StringProperty breadCrumbProperty() {
    return breadCrumb.textProperty();
  }
}
