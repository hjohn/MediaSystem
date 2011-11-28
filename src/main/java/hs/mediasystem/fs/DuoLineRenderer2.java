package hs.mediasystem.fs;

import hs.mediasystem.framework.MediaItem;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class DuoLineRenderer2 extends ListCell<MediaItem> {
  
  private final HBox group = new HBox();
  
  private final Label title = new Label() {{
    setId("selectItem-listCell-title");
  }};
  
  private final Label subtitle = new Label() {{
    setId("selectItem-listCell-subtitle");
  }};

  private Group collectionMarker = new Group() {{
    getChildren().add(new Path() {{
      getElements().addAll(
        new MoveTo(0, 0),
        new LineTo(0, 30),
        new LineTo(15, 15),
        new LineTo(0, 0)
      );
    }});
  }};

  public DuoLineRenderer2() {
    group.getChildren().add(new VBox() {{
      getChildren().add(title);
      getChildren().add(subtitle);
      HBox.setHgrow(this, Priority.ALWAYS);
    }});
    group.getChildren().add(collectionMarker);
    
    setGraphic(group);
  }
  
  @Override
  protected void updateItem(MediaItem item, boolean empty) {
    super.updateItem(item, empty);

    if(item != null) {
      title.setText(item.getTitle());
      subtitle.setText(item.getSubtitle());
      collectionMarker.setVisible(item instanceof hs.mediasystem.framework.Group);
    }
  }
}