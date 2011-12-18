package hs.mediasystem.screens;

import hs.mediasystem.ProgramController;
import hs.mediasystem.StringConverter;

import java.util.List;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.WritableNumberValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
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
  private final ObservableList<Option> options;
  
  @Override
  public void start(Stage stage) throws Exception {
    ObservableList<Option> options = FXCollections.observableArrayList(
      new NumericOption("Volume", "%3.0f%%", 1, 0, 100), 
      new Option("Audio Stream"),
      new NumericOption("Audio Delay", "%4.1fs", 0.1) 
    );
    DialogScreen dialogScreen = new DialogScreen(null, options);
    
    BorderPane borderPane = new BorderPane();
    
    borderPane.setCenter(dialogScreen.create());
        
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
    this.options = null;
  }
  
  public DialogScreen(ProgramController controller, ObservableList<Option> options) {
    this.controller = controller;
    this.options = options;
  }
  
  /*
  
  Option 'subtitle delay' bind subtitle delay property
  
  
  
  
  
  
  */
  
  
  
  public Node create() {

    BorderPane borderPane = new BorderPane();
    borderPane.setId("dialog-screen");

    VBox box = new VBox();
    
    box.setMaxSize(800, 600);
    
    box.setId("dialog");
    
    for(Option option : options) {
      box.getChildren().add(option.getControl());
    }
    
//    return new ListView<Option>(list) {{
//      this.setCellFactory(new MyCellFactoryCallBack());
//    }};
    
    borderPane.setCenter(box);
    return borderPane;
  }
  
  public static class Option {
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
          getStyleClass().add("cell");
          
          
//          addEventHandler(EventType.ROOT, new EventHandler<Event>() {
//            @Override
//            public void handle(Event event) {
//              System.out.println("Received event : " + event);
//            }
//          });

          addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            final KeyCombination tab = new KeyCodeCombination(KeyCode.TAB);
            final KeyCombination shiftTab = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
            final KeyCombination down = new KeyCodeCombination(KeyCode.DOWN);
            final KeyCombination up = new KeyCodeCombination(KeyCode.UP);

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
              else if(tab.match(event) || down.match(event)) {
                moveFocusNext(borderPane);
              }
              else if(shiftTab.match(event) || up.match(event)) {
                moveFocusPrevious(borderPane);
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
  
  public static class ListOption<T> extends Option {
    private final Label control = new Label();
    private final ObjectProperty<T> property;
    private final List<T> items;
    private final StringConverter<T> stringConverter;

    private T value;
    
    public ListOption(String description, ObjectProperty<T> property, List<T> items, StringConverter<T> stringConverter) {
      super(description);
      this.property = property;
      this.items = items;
      this.stringConverter = stringConverter;
      this.value = property.get();
      
      updateControl();
    }
    
    @Override
    public void left() {
      int index = items.indexOf(value) - 1;
      
      if(index < 0) {
        index = items.size() - 1;
      }
      
      value = items.get(index);
      
      updateControl();
    }
    
    @Override
    public void right() {
      int index = items.indexOf(value) + 1;
      
      if(index >= items.size()) {
        index = 0;
      }
      
      value = items.get(index);
      
      updateControl();
    }
    
    private void updateControl() {
      if(property != null) {
        property.set(value);
      }
      control.setText(stringConverter.toString(value));
    }
    
    @Override
    public Node getRightControl() {
      return control;
    }
  }
  
  public static class NumericOption extends Option {
    private final WritableNumberValue property;
    private final Label control = new Label();
    private final String format;
    private final double stepSize;
    private final double min;
    private final double max;
    
    private double value;

    public NumericOption(WritableNumberValue property, String description, String format, double stepSize, double min, double max) {
      super(description);
      this.property = property;
      this.format = format;
      this.stepSize = stepSize;
      this.min = min;
      this.max = max;
      
      if(property != null) {
        value = property.getValue().doubleValue();
      }
      
      updateControl();
    }
    
    public NumericOption(String description, String format, double stepSize, double min, double max) {
      this(null, description, format, stepSize, min, max);
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
      if(property != null) {
        property.setValue(value);
      }
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
  
  private static void moveFocusPrevious(BorderPane borderPane) {
    ObservableList<Node> parentChildren = borderPane.getParent().getChildrenUnmodifiable();
    int indexInParent = parentChildren.indexOf(borderPane);
    
    if(indexInParent == 0) {
      parentChildren.get(parentChildren.size() - 1).requestFocus();
    }
    else {
      parentChildren.get(indexInParent - 1).requestFocus();
    }
  }

}
