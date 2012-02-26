package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.SelectMediaView;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.Events;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

public abstract class AbstractDuoPaneSelectMediaView extends StackPane implements SelectMediaView {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final ListPane listPane;

  public AbstractDuoPaneSelectMediaView(ListPane listPane, DetailPane detailPane, BackgroundPane backgroundPane) {
    getStylesheets().add("select-media/duo-pane-select-media-view.css");

    detailPane.getStyleClass().add("content-box");
    listPane.getStyleClass().add("content-box");

    this.listPane = listPane;

    final GridPane root = new GridPane();

    root.getStyleClass().addAll("content-box-grid");

    addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          Events.dispatchEvent(onBack, new ActionEvent(AbstractDuoPaneSelectMediaView.this, null), event);
        }
      }
    });

    root.getColumnConstraints().addAll(
      new ColumnConstraints() {{
        setPercentWidth(50);
      }},
      new ColumnConstraints() {{
        setPercentWidth(50);
      }}
    );

    root.getRowConstraints().addAll(
      new RowConstraints() {{
        setPercentHeight(25);
      }},
      new RowConstraints() {{
        setPercentHeight(75);
      }}
    );

    detailPane.mediaItemProperty().bind(listPane.mediaItemBinding());

    root.add(detailPane, 0, 1);
    root.add((Node)listPane, 1, 1);

    backgroundPane.mediaItemProperty().bind(listPane.mediaItemBinding());

    getChildren().add(backgroundPane);
    getChildren().add(root);
  }

  @Override
  public void requestFocus() {
    listPane.requestFocus();
  }

  @Override
  public void setRoot(final MediaNode root) {
    listPane.setRoot(root);
  }

  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onItemSelected() { return listPane.onItemSelected(); }
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onItemAlternateSelect() { return listPane.onItemAlternateSelect(); }

  private final ObjectProperty<EventHandler<ActionEvent>> onBack = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<ActionEvent>> onBack() { return onBack; }
}
