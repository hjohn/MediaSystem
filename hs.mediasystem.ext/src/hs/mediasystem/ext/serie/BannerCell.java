package hs.mediasystem.ext.serie;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.enrich.EnrichTrigger;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaNodeCell;
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

  private final Label title = new Label() {{
    setId("selectItem-listCell-title");
  }};

  public BannerCell() {
    getChildren().add(new VBox() {{
      getChildren().add(title);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
  }

  @Override
  public void configureCell(MediaNode mediaNode) {
    final EnrichTrigger item = mediaNode.getMediaItem();

    binder.unbindAll();

    // A banner from TVDB is 758 x 140

    if(item != null) {
      final AsyncImageProperty asyncImageProperty = new AsyncImageProperty();
      final StringProperty titleProperty = new SimpleStringProperty();

      ObjectBinding<ImageHandle> banner = MapBindings.select(mediaNode.dataMapProperty(), Media.class, "banner");

      binder.bind(titleProperty, MapBindings.selectString(mediaNode.dataMapProperty(), Media.class, "title"));
      binder.bind(asyncImageProperty.imageHandleProperty(), banner);

      title.minHeightProperty().bind(minWidthProperty().multiply(140).divide(758));

      binder.bind(title.textProperty(), Bindings.when(asyncImageProperty.isNull()).then(titleProperty).otherwise(""));
      binder.bind(title.graphicProperty(), Bindings.when(asyncImageProperty.isNull()).then((ImageView)null).otherwise(new ImageView() {{
        imageProperty().bind(asyncImageProperty);
        setPreserveRatio(true);
        fitWidthProperty().bind(BannerCell.this.minWidthProperty());
      }}));
    }
  }
}
