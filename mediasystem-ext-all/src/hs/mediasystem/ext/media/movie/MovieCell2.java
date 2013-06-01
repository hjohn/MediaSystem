package hs.mediasystem.ext.media.movie;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.WeakBinder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class MovieCell2 extends StackPane implements MediaNodeCell {
  private final WeakBinder binder = new WeakBinder();

  private final Label title = new Label() {{
    setId("selectItem-listCell-title");
  }};

  public MovieCell2() {
    getChildren().add(title);
    setAlignment(Pos.CENTER);
    title.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//    HBox.setHgrow(title, Priority.NEVER);
  }

  @Override
  public void attach(MediaNode mediaNode) {
    final MediaItem item = mediaNode.getMediaItem();

    // A banner from TVDB is 758 x 140

    if(item != null) {
      final AsyncImageProperty asyncImageProperty = new AsyncImageProperty();
   //   final StringProperty titleProperty = new SimpleStringProperty();

      ObjectBinding<ImageHandle> poster = MapBindings.select(mediaNode.media, "image");

    //  binder.bind(titleProperty, MapBindings.selectString(mediaNode.media, "title"));
      binder.bind(asyncImageProperty.imageHandleProperty(), poster);

      //title.minHeightProperty().bind(minWidthProperty().divide(2));

    //  binder.bind(title.textProperty(), Bindings.when(asyncImageProperty.isNull()).then(titleProperty).otherwise(""));
      binder.bind(title.graphicProperty(), Bindings.when(asyncImageProperty.isNull()).then((ImageView)null).otherwise(new ImageView() {{
        imageProperty().bind(asyncImageProperty);
        setPreserveRatio(true);
        setSmooth(true);
        //fitWidthProperty().bind(MovieCell2.this.minWidthProperty());
//        fitWidthProperty().set(500);
//        fitHeightProperty().set(500);
      }}));
    }
  }

  @Override
  public void detach() {
    binder.unbindAll();
  }

//  @Override
//  protected double computePrefWidth(double height) {
//    return 300;
//  }
//
//  @Override
//  protected double computePrefHeight(double width) {
//    return 400;
//  }
}