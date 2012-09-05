package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.GridPaneUtil;
import hs.mediasystem.util.MapBindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.osgi.framework.BundleContext;

public abstract class AbstractDuoPaneStandardLayout extends StackPane implements StandardLayout {
  private final ListPane listPane;

  public AbstractDuoPaneStandardLayout(BundleContext bundleContext, final ListPane listPane) {
    this.listPane = listPane;

    final GridPane root = GridPaneUtil.create(new double[] {100}, new double[] {17, 75, 8});
    final GridPane panelGroup = GridPaneUtil.create(new double[] {50, 50}, new double[] {100});

    panelGroup.getStyleClass().addAll("content-box-grid");

    root.add(panelGroup, 0, 1);

    panelGroup.setEffect(new Reflection(5, 0.025, 0.25, 0.0));

    final StackPane listPaneContainer = new StackPane();
    final DetailPane detailPane = createDetailPane(bundleContext);

    detailPane.contentProperty().bind(MapBindings.select(listPane.mediaNodeBinding(), "dataMap", Media.class));

    listPaneContainer.getStyleClass().add("box-content");
    detailPane.getStyleClass().add("box-content");

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
      getChildren().add(listPaneContainer);
    }}, 1, 0);

    listPaneContainer.getChildren().add((Node)listPane);

    getChildren().add(root);
  }

  private static DetailPane createDetailPane(BundleContext bundleContext) {
    return new DetailPane(bundleContext) {
      @Override
      protected void initialize(DecoratablePane decoratablePane) {
        decoratablePane.getChildren().add(new BorderPane() {{
          setTop(new VBox() {{
            setId("title-area");
          }});

          setCenter(new GridPane() {{
            GridPaneUtil.configure(this, new double[] {60, 40}, new double[] {100});

            setHgap(20);

            add(new VBox() {{
              setId("description-area");
            }}, 0, 0);

            add(new VBox() {{
              setId("title-image-area");
            }}, 1, 0);
          }});

          setBottom(new HBox() {{
            setId("link-area");
          }});
        }});
      }
    };
  }

  @Override
  public void requestFocus() {
    listPane.requestFocus();
  }

  @Override
  public MediaNode getRoot() {
    return listPane.getRoot();
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
  @Override public ReadOnlyObjectProperty<MediaNode> focusedNodeProperty() { return listPane.focusedNodeProperty(); }
}
