package hs.mediasystem.screens.collection;

import hs.mediasystem.util.GridPaneUtil;

import java.util.Set;

import javax.inject.Inject;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SmallDetailPane extends AbstractDetailPane {

  @Inject
  public SmallDetailPane(Set<DetailPaneDecoratorFactory> detailPaneDecoratorFactories) {
    super(detailPaneDecoratorFactories);
  }

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
}