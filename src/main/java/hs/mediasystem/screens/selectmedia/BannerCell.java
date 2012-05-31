package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaNodeCell;
import hs.mediasystem.media.Media;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.WeakBinder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class BannerCell extends HBox implements MediaNodeCell {
  private final WeakBinder binder = new WeakBinder();
  private final int fitWidth;

  private final Label title = new Label() {{
    setId("selectItem-listCell-title");
  }};

  public BannerCell(int fitWidth) {
    this.fitWidth = fitWidth;

    getChildren().add(new VBox() {{
      getChildren().add(title);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
  }

  public BannerCell() {
    this(350);
  }

  @Override
  public void configureCell(MediaNode mediaNode) {
    final MediaItem item = mediaNode.getMediaItem();

    binder.unbindAll();

    // A banner from TVDB is 758 x 140

    if(item != null) {
      final AsyncImageProperty asyncImageProperty = new AsyncImageProperty();
      final StringProperty titleProperty = new SimpleStringProperty();

      ObjectBinding<ImageHandle> banner = MapBindings.select(mediaNode.dataMapProperty(), Media.class, "banner");

      binder.bind(titleProperty, MapBindings.selectString(mediaNode.dataMapProperty(), Media.class, "title"));
      binder.bind(asyncImageProperty.imageHandleProperty(), banner);

      title.setMinHeight(fitWidth * 140 / 758);

      binder.bind(title.textProperty(), Bindings.when(asyncImageProperty.isNull()).then(titleProperty).otherwise(""));
      binder.bind(title.graphicProperty(), Bindings.when(asyncImageProperty.isNull()).then((ImageView)null).otherwise(new ImageView() {{
        imageProperty().bind(asyncImageProperty);
        setPreserveRatio(true);
        setFitWidth(fitWidth);
      }}));
    }
  }
}
