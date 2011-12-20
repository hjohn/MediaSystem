package hs.mediasystem.screens;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class DialogScreen {
  private final ObservableList<Option> options;
  private final String title;
  
  public DialogScreen(String title, ObservableList<Option> options) {
    this.title = title;
    this.options = options;
  }
  
  public Node create() {
    VBox box = new VBox() {{
      setId("dialog");
      setMaxSize(800, 600);
      
      getChildren().add(new Label(title) {{
        getStyleClass().add("title");
        setMaxWidth(Integer.MAX_VALUE);
      }});
      
      getChildren().add(new VBox() {{
        for(Option option : options) {
          getChildren().add(option.getControl());
        }
      }});  
    }};

    BorderPane borderPane = new BorderPane();
    
    borderPane.setId("dialog-screen");
    borderPane.setCenter(box);

    return borderPane;
  }
}
