package hs.mediasystem.screens;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DialogScreen {
  private final ObservableList<Option> options;
  
  public DialogScreen(ObservableList<Option> options) {
    this.options = options;
  }
  
  public Node create() {
    VBox box = new VBox() {{
      setId("dialog");
      setMaxSize(800, 600);
      
      for(Option option : options) {
        getChildren().add(option.getControl());
      }
    }};

    BorderPane borderPane = new BorderPane();
    
    borderPane.setId("dialog-screen");
    borderPane.setCenter(box);

    return borderPane;
  }
}
