package hs.mediasystem.screens;

import hs.mediasystem.ProgramController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DialogScreen extends Application {
  private final ProgramController controller;
  
  @Override
  public void start(Stage stage) throws Exception {
    BorderPane borderPane = new BorderPane();
    
    borderPane.setCenter(create());
        
    setupStage(stage, borderPane, 1.0);
    
    stage.show();
  }
  
  private static void setupStage(Stage stage, BorderPane borderPane, double transparency) {
    Scene scene = new Scene(borderPane, 600, 400, new Color(0, 0, 0, transparency));
    
    stage.setScene(scene);
    
//    Screen screen = Screen.getPrimary();
//    Rectangle2D bounds = screen.getVisualBounds();
//
//    stage.setX(bounds.getMinX());
//    stage.setY(bounds.getMinY());
//    stage.setWidth(bounds.getWidth());
//    stage.setHeight(bounds.getHeight());

    scene.getStylesheets().add("default.css");
  }

  public static void main(String[] args) {
    DialogScreen.launch(new String[] {});
  }
  
  public DialogScreen() {
    this.controller = null;
  }
  
  public DialogScreen(ProgramController controller) {
    this.controller = controller;
  }
  
  /*
  
  Option 'subtitle delay' bind subtitle delay property
  
  
  
  
  
  
  */
  
  
  
  public Node create() {
    ObservableList<Option> list = FXCollections.observableArrayList(
      new NumericOption("Volume", "%3.0f%%", 1, 0, 100), 
      new Option("Audio Stream"),
      new NumericOption("Audio Delay", "%4.1fs", 0.1) 
    );
    
    VBox box = new VBox();
    
    for(Option option : list) {
      box.getChildren().add(option.getControl());
    }
    
//    return new ListView<Option>(list) {{
//      this.setCellFactory(new MyCellFactoryCallBack());
//    }};
    
    
    return box;
  }
  
  public class Option {
    private final String description;
    private BorderPane borderPane;

    public Option(String description) {
      this.description = description;
    }
    
    public String getDescription() {
      return description;
    }

    public Node getControl() {
      if(borderPane == null) {
        borderPane = new BorderPane() {{
          setFocusTraversable(true);
          getStyleClass().add("myborderpane");
          
          
//          addEventHandler(EventType.ROOT, new EventHandler<Event>() {
//            @Override
//            public void handle(Event event) {
//              System.out.println("Received event : " + event);
//            }
//          });

          addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            final KeyCombination tab = new KeyCodeCombination(KeyCode.TAB);
            final KeyCombination shiftTab = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);

            @Override
            public void handle(KeyEvent event) {
              if(event.getCode() == KeyCode.LEFT) {
                System.out.println("left");
                left();
                event.consume();
              }
              else if(event.getCode() == KeyCode.RIGHT) {
                System.out.println("right");
                right();
                event.consume();
              }
              else if(tab.match(event)) {
                moveFocusNext(borderPane);
                
              }
              else if(shiftTab.match(event)) {
              }
            }
          });
          setLeft(new Label(getDescription()));
          setRight(getRightControl());
        }};
      }
      
      return borderPane;
    }
    
    public void left() {
    }
    
    public void right() {
    }
    
    public Node getRightControl() {
      return new Label("<Value>");
    }
  }
  
  public class NumericOption extends Option {
    private final Label control = new Label("--Val--");
    private final String format;
    private final double stepSize;
    private final double min;
    private final double max;
    
    private double value;

    public NumericOption(String description, String format, double stepSize, double min, double max) {
      super(description);
      this.format = format;
      this.stepSize = stepSize;
      this.min = min;
      this.max = max;
    }
    
    public NumericOption(String description, String format, double stepSize) {
      this(description, format, stepSize, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Override
    public void left() {
      value -= stepSize;
      
      if(value < min) {
        value = min;
      }
      
      updateControl();
    }
    
    @Override
    public void right() {
      value += stepSize;
      
      if(value > max) {
        value = max;
      }
      
      updateControl();
    }
    
    private void updateControl() {
      control.setText(String.format(format, value));
    }
    
    @Override
    public Node getRightControl() {
      return control;
    }
  }
  
  private static void moveFocusNext(BorderPane borderPane) {
    ObservableList<Node> parentChildren = borderPane.getParent().getChildrenUnmodifiable();
    int indexInParent = parentChildren.indexOf(borderPane);
    
    if(indexInParent + 1 >= parentChildren.size()) {
      parentChildren.get(0).requestFocus();
    }
    else {
      parentChildren.get(indexInParent + 1).requestFocus();
    }
  }
}
