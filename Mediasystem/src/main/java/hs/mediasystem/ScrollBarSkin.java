package hs.mediasystem;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.shape.Rectangle;

public class ScrollBarSkin implements Skin<ScrollBar> {

  private final ScrollBar scrollBar;

  public ScrollBarSkin(ScrollBar scrollBar) {
    this.scrollBar = scrollBar;
  }

  @Override
  public void dispose() {
  }

  @Override
  public Node getNode() {
    return new Group() {{
      getChildren().add(new Rectangle() {{
        getStyleClass().add("track");
        if(scrollBar.getOrientation() == Orientation.HORIZONTAL) {
          widthProperty().bind(scrollBar.widthProperty());
          setHeight(16);
        }
        else {
          setWidth(16);
          heightProperty().bind(scrollBar.heightProperty());
        }
      }});
      getChildren().add(new Rectangle() {{
        getStyleClass().add("thumb");

        NumberBinding range = Bindings.subtract(scrollBar.maxProperty(), scrollBar.minProperty());
        NumberBinding position = Bindings.divide(Bindings.subtract(scrollBar.valueProperty(), scrollBar.minProperty()), range);

        if(scrollBar.getOrientation() == Orientation.HORIZONTAL) {
          setHeight(16);
          widthProperty().bind(scrollBar.visibleAmountProperty().divide(range).multiply(scrollBar.widthProperty()));
          xProperty().bind(Bindings.subtract(scrollBar.widthProperty(), widthProperty()).multiply(position));
        }
        else {
          setWidth(16);
          heightProperty().bind(scrollBar.visibleAmountProperty().divide(range).multiply(scrollBar.heightProperty()));
          yProperty().bind(Bindings.subtract(scrollBar.heightProperty(), heightProperty()).multiply(position));
        }
      }});
    }};
  }

  @Override
  public ScrollBar getSkinnable() {
    return scrollBar;
  }
}
