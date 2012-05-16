package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.GridPaneUtil;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public abstract class AbstractDuoPaneStandardLayout extends StackPane implements StandardLayout {
  private final ListPane listPane;

  public AbstractDuoPaneStandardLayout(final ListPane listPane, final DetailPane detailPane) {
    detailPane.getStyleClass().add("box-content");
    listPane.getStyleClass().add("box-content");

    this.listPane = listPane;

    final GridPane root = GridPaneUtil.create(new double[] {100}, new double[] {17, 75, 8});
    final GridPane panelGroup = GridPaneUtil.create(new double[] {50, 50}, new double[] {100});

    panelGroup.getStyleClass().addAll("content-box-grid");

    detailPane.mediaNodeProperty().bind(listPane.mediaNodeBinding());

    root.add(panelGroup, 0, 1);

    detailPane.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 3, 0.0, 0, 0));
    ((Node)listPane).setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 3, 0.0, 0, 0));

    panelGroup.setEffect(new Reflection(5, 0.025, 0.25, 0.0));

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
  @Override public ObjectBinding<MediaNode> mediaNodeBinding() { return listPane.mediaNodeBinding(); }
}
