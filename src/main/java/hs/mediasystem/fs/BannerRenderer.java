package hs.mediasystem.fs;

import hs.mediasystem.ImageCache;
import hs.mediasystem.ImageHandle;
import hs.mediasystem.framework.MediaItem;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BannerRenderer implements CellProvider<MediaItem> {
  private final HBox group = new HBox();

  private final Label title = new Label() {{
    setId("selectItem-listCell-title");
  }};

  public BannerRenderer() {
    group.getChildren().add(new VBox() {{
      getChildren().add(title);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
  }

  @Override
  public Node configureCell(MediaItem item) {
    if(item != null) {
      ImageHandle banner = item.getBanner();

      if(banner != null) {
        title.setGraphic(new ImageView(ImageCache.loadImage(banner)) {{
          setPreserveRatio(true);
          setFitHeight(60);  // TODO No absolute values
        }});
        title.setText("");
      }
      else {
        title.setGraphic(null);
        title.setText(item.getTitle());
      }
    }

    return group;
  }
}
