package hs.mediasystem.framework.actions.controls;

import hs.mediasystem.config.Resources;
import hs.mediasystem.framework.actions.ActionTarget;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class ButtonControlFactory implements ControlFactory<Object> {

  @Override
  public Node[] create(ActionTarget actionTarget, Object parent) {
    Button button = new Button(Resources.getResource(actionTarget.getMemberName(), "trigger.label"));

    button.setOnAction(event -> {
      actionTarget.getExposedMember().doAction("trigger", parent, event);
    });

    return new Node[] {button};
  }
}
