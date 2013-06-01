package hs.mediasystem.controls;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ControlTest extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    ObservableList<String> items = FXCollections.observableArrayList("Terminator", "Stargate Atlantis", "Star Wars: The Clone Wars", "Star Trek: Voyager", "Stargate Universe");

    HBox box = new HBox();

    TreeView<String> treeView = new TreeView<>();

    TreeItem<String> treeItem = new TreeItem<>("Root");

    for(String item : items) {
      treeItem.getChildren().add(new TreeItem<>(item));
    }

    treeView.setRoot(treeItem);
    treeView.setShowRoot(false);

    box.getChildren().add(treeView);
    box.getChildren().add(new Label("Select an item, deselect the window, then hover over the items with mouse.  Item size changes.  Doesn't happen when you donot set the font-size.") {{
      setPrefWidth(100);
      setWrapText(true);
    }});

    Scene scene = new Scene(box);

    scene.getStylesheets().add("default-copy.css");

    stage.setScene(scene);
    stage.show();
  }
}
