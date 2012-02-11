package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.ImageHandle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BannerCellProvider implements CellProvider<MediaItem> {
  private final HBox group = new HBox();

  private final Label title = new Label() {{
    setId("selectItem-listCell-title");
  }};

  public BannerCellProvider() {
    group.getChildren().add(new VBox() {{
      getChildren().add(title);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
  }

  @Override
  public Node configureCell(TreeItem<MediaItem> treeItem) {
    final MediaItem item = treeItem.getValue();

    if(item != null) {
      title.textProperty().bind(Bindings.when(item.bannerProperty().isNull()).then(item.titleProperty()).otherwise(""));
      title.graphicProperty().bind(Bindings.when(item.bannerProperty().isNull()).then((ImageView)null).otherwise(new ObjectBinding<ImageView>() {
        {
          bind(item.bannerProperty());
        }

        @Override
        protected ImageView computeValue() {
          ImageHandle banner = item.getBanner();

          return banner == null ? null : new ImageView(ImageCache.loadImage(banner)) {{
            setPreserveRatio(true);
            setFitHeight(60);  // TODO No absolute values
          }};
        }
      }));
    }

    return group;
  }
}
