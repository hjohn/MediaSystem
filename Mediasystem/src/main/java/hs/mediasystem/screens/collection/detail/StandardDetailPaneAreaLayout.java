package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.screens.AreaLayout;
import hs.mediasystem.util.AreaPane;
import hs.mediasystem.util.GridPaneUtil;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StandardDetailPaneAreaLayout implements AreaLayout {

  @Override
  public void layout(AreaPane areaPane) {
    areaPane.getChildren().add(new BorderPane() {{
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
