package hs.mediasystem.screens;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import javafx.beans.binding.Bindings;
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
      final AsyncImageProperty asyncImageProperty = new AsyncImageProperty(item.bannerProperty());
      title.textProperty().bind(Bindings.when(asyncImageProperty.isNull()).then(item.titleProperty()).otherwise(""));
      title.graphicProperty().bind(Bindings.when(asyncImageProperty.isNull()).then((ImageView)null).otherwise(new ImageView() {{
        imageProperty().bind(asyncImageProperty);
        setPreserveRatio(true);
        setFitHeight(60);  // TODO No absolute values
      }}));
    }

    return group;
  }
}
