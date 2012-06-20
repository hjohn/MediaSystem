package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.GridPaneUtil;
import hs.mediasystem.util.PropertyEq;
import hs.mediasystem.util.ServiceTracker;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import org.osgi.framework.BundleContext;

public abstract class AbstractDuoPaneStandardLayout extends StackPane implements StandardLayout {
  private final ListPane listPane;

  public AbstractDuoPaneStandardLayout(BundleContext bundleContext, final ListPane listPane) {
    final ServiceTracker<DetailPane> detailPaneTracker = new ServiceTracker<>(bundleContext, DetailPane.class);

    this.listPane = listPane;

    final GridPane root = GridPaneUtil.create(new double[] {100}, new double[] {17, 75, 8});
    final GridPane panelGroup = GridPaneUtil.create(new double[] {50, 50}, new double[] {100});

    panelGroup.getStyleClass().addAll("content-box-grid");

    root.add(panelGroup, 0, 1);

    panelGroup.setEffect(new Reflection(5, 0.025, 0.25, 0.0));

    final StackPane listPaneContainer = new StackPane();
    final StackPane detailPaneContainer = new StackPane();

    listPaneContainer.getStyleClass().add("box-content");
    detailPaneContainer.getStyleClass().add("box-content");

    panelGroup.add(new StackPane() {{
      getChildren().add(new StackPane() {{
        getStyleClass().add("box");
        setEffect(new Lighting(new Light.Distant(-135.0, 30.0, Color.WHITE)) {{
          setSpecularConstant(1.5);
          setSurfaceScale(3.0);
        }});
      }});
      getChildren().add(detailPaneContainer);
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

    mediaNodeBinding().addListener(new ChangeListener<MediaNode>() {
      private DetailPane currentDetailPane;

      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {
        if(currentDetailPane != null) {
          detailPaneContainer.getChildren().clear();
          currentDetailPane.mediaNodeProperty().unbind();
        }

        if(current != null) {
          DetailPane detailPane = null;
          Class<?> cls = current.getDataType();

          while(detailPane == null) {
            detailPane = detailPaneTracker.getService(new PropertyEq("mediasystem.class", cls));
            cls = cls.getSuperclass();
          }

          detailPane.mediaNodeProperty().bind(listPane.mediaNodeBinding());
          detailPaneContainer.getChildren().add((Node)detailPane);

          currentDetailPane = detailPane;
        }
      }
    });
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
