package hs.mediasystem.framework.actions.controls;

import hs.mediasystem.config.Resources;
import hs.mediasystem.framework.actions.ActionTarget;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;

public class CheckBoxControlFactory implements ControlFactory<Boolean> {

  @Override
  public Node[] create(ActionTarget actionTarget, Object parent) {
    CheckBox checkBox = new CheckBox(Resources.getResource(actionTarget.getMemberName(), "checkBox.label"));

    checkBox.selectedProperty().bindBidirectional(actionTarget.getProperty(parent));

    return new Node[] {checkBox};
  }
}
