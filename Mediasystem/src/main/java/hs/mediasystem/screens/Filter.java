package hs.mediasystem.screens;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

public class Filter extends FlowPane {
  private final ObjectProperty<Node> active = new SimpleObjectProperty<>();
  public ObjectProperty<Node> activeProperty() { return active; }

  public Filter() {
    getStyleClass().add("filter");
    getChildren().addListener(new ListChangeListener<Node>() {
      @Override
      public void onChanged(ListChangeListener.Change<? extends Node> event) {
        update();
      }
    });

    active.addListener(new ChangeListener<Node>() {
      @Override
      public void changed(ObservableValue<? extends Node> arg0, Node arg1, Node arg2) {
        update();
      }
    });
  }

  public void activateNext() {
    if(!getChildren().isEmpty()) {
      int index = getChildren().indexOf(active.get());

      index++;

      if(index >= getChildren().size()) {
        index = 0;
      }

      active.set(getChildren().get(index));
    }
  }

  public void activatePrevious() {
    if(!getChildren().isEmpty()) {
      int index = getChildren().indexOf(active.get());

      index--;

      if(index < 0) {
        index = getChildren().size() - 1;
      }

      active.set(getChildren().get(index));
    }
  }

  private void update() {
    Node activeNode = this.active.get();

    for(Node node : getChildren()) {
      node.setDisable(!node.equals(activeNode));
    }
  }
}