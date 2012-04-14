package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.SelectMediaView;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.GridPaneUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.Reflection;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public abstract class AbstractDuoPaneSelectMediaView extends StackPane implements SelectMediaView {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final ListPane listPane;

  public AbstractDuoPaneSelectMediaView(final ListPane listPane, final DetailPane detailPane, BackgroundPane backgroundPane) {
    getStylesheets().add("select-media/duo-pane-select-media-view.css");

    detailPane.getStyleClass().add("box-content");
    listPane.getStyleClass().add("box-content");

    this.listPane = listPane;

    final GridPane root = GridPaneUtil.create(new double[] {100}, new double[] {17, 75, 8});
    final GridPane panelGroup = GridPaneUtil.create(new double[] {50, 50}, new double[] {100});
    final GridPane stage = GridPaneUtil.create(new double[] {100}, new double[] {90, 10});

    panelGroup.getStyleClass().addAll("content-box-grid");

    addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          Events.dispatchEvent(onBack, new ActionEvent(AbstractDuoPaneSelectMediaView.this, null), event);
        }
      }
    });

    detailPane.mediaItemProperty().bind(listPane.mediaItemBinding());

    root.add(panelGroup, 0, 1);

    detailPane.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 3, 0.0, 0, 0));
    ((Node)listPane).setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 3, 0.0, 0, 0));

    panelGroup.setEffect(new Reflection(5, 0.1, 0.25, 0.0));

    panelGroup.add(new StackPane() {{
      getChildren().add(new StackPane() {{
        getStyleClass().add("box");
        setEffect(new Lighting(new Light.Distant(-135.0, 30.0, Color.WHITE)) {{
          setSpecularConstant(1.5);
          setSurfaceScale(3.0);
        }});
      }});
      getChildren().add(detailPane);
    }}, 0, 0);

    panelGroup.add(new StackPane() {{
      getChildren().add(new StackPane() {{
        getStyleClass().add("box");
        setEffect(new Lighting(new Light.Distant(-135.0, 30.0, Color.WHITE)) {{
          setSpecularConstant(1.5);
          setSurfaceScale(2.0);
        }});
      }});
      getChildren().add((Node)listPane);
    }}, 1, 0);

    stage.add(new StackPane() {{
      getStyleClass().add("stage");
    }}, 0, 1);

    backgroundPane.mediaItemProperty().bind(listPane.mediaItemBinding());

    getChildren().add(backgroundPane);
    getChildren().add(stage);
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

  @Override
  public MediaNode getSelectedNode() {
    return listPane.getSelectedNode();
  }

  @Override
  public void setSelectedNode(MediaNode mediaNode) {
    listPane.setSelectedNode(mediaNode);
  }

  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected() { return listPane.onNodeSelected(); }
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect() { return listPane.onNodeAlternateSelect(); }

  private final ObjectProperty<EventHandler<ActionEvent>> onBack = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<ActionEvent>> onBack() { return onBack; }
}
