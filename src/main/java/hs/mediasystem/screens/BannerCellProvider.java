package hs.mediasystem.screens;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.WeakBinder;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BannerCellProvider implements CellProvider<MediaNode> {
  private final HBox group = new HBox();
  private final int fitWidth;
  private final WeakBinder binder = new WeakBinder();

  private final Label title = new Label() {{
    setId("selectItem-listCell-title");
  }};

  public BannerCellProvider(int fitWidth) {
    this.fitWidth = fitWidth;

    group.getChildren().add(new VBox() {{
      getChildren().add(title);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
  }

  public BannerCellProvider() {
    this(350);
  }

  @Override
  public Node configureCell(MediaNode mediaNode) {
    final MediaItem item = mediaNode.getMediaItem();

    binder.unbindAll();

    // A banner from TVDB is 758 x 140

    if(item != null) {
      final AsyncImageProperty asyncImageProperty = new AsyncImageProperty(item.bannerProperty());

      title.setMinHeight(fitWidth * 140 / 758);

      binder.bind(title.textProperty(), Bindings.when(asyncImageProperty.isNull()).then(item.titleProperty()).otherwise(""));
      binder.bind(title.graphicProperty(), Bindings.when(asyncImageProperty.isNull()).then((ImageView)null).otherwise(new ImageView() {{
        imageProperty().bind(asyncImageProperty);
        setPreserveRatio(true);
        setFitWidth(fitWidth);
      }}));
    }

    return group;
  }
}
