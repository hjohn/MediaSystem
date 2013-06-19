package hs.mediasystem.screens.collection;

import hs.mediasystem.util.GridPaneUtil;
import javafx.scene.Node;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class DuoPaneCollectionSelector extends StackPane {
  private final StackPane left = new StackPane();
  private final StackPane right = new StackPane();

  public DuoPaneCollectionSelector() {
    final GridPane root = GridPaneUtil.create(new double[] {100}, new double[] {17, 75, 8});
    final GridPane panelGroup = GridPaneUtil.create(new double[] {50, 50}, new double[] {100});

    getStylesheets().add("collection/duo-pane-collection-selector-layout.css");

    panelGroup.getStyleClass().addAll("content-box-grid");

    root.add(panelGroup, 0, 1);

    panelGroup.setEffect(new Reflection(5, 0.025, 0.25, 0.0));

    left.getStyleClass().add("box-content");
    right.getStyleClass().add("box-content");

    StackPane leftRoot = new StackPane() {{
      getChildren().add(new StackPane() {{
        getStyleClass().add("box");
        setEffect(new Lighting(new Light.Distant(-135.0, 30.0, Color.WHITE)) {{
          setSpecularConstant(1.5);
          setSurfaceScale(3.0);
        }});
      }});
      getChildren().add(left);
    }};

    StackPane rightRoot = new StackPane() {{
      getChildren().add(new StackPane() {{
        getStyleClass().add("box");
        setEffect(new Lighting(new Light.Distant(-135.0, 30.0, Color.WHITE)) {{
          setSpecularConstant(1.5);
          setSurfaceScale(3.0);
        }});
      }});
      getChildren().add(right);
    }};

    panelGroup.add(leftRoot, 0, 0);
    panelGroup.add(rightRoot, 1, 0);

    getChildren().add(root);
  }

  public void placeLeft(Node node) {
    left.getChildren().setAll(node);
  }

  public void placeRight(Node node) {
    right.getChildren().setAll(node);
  }
}
