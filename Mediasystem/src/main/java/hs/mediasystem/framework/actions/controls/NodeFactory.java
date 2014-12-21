package hs.mediasystem.framework.actions.controls;

import hs.mediasystem.framework.actions.ActionTarget;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;

public class NodeFactory {
  private static final Map<String, ControlFactory<?>> controlFactoriesByType = new HashMap<>();

  static {
    controlFactoriesByType.put("slider", new SliderControlFactory());
    controlFactoriesByType.put("checkBox", new CheckBoxControlFactory());
    controlFactoriesByType.put("comboBox", new ComboBoxControlFactory());
    controlFactoriesByType.put("trigger", new ButtonControlFactory());
  }

  private final ActionTarget actionTarget;
  private final String controlType;
  private final ControlFactory<Object> controlFactory;

  @SuppressWarnings("unchecked")
  public NodeFactory(ActionTarget actionTarget, String controlType) {
    this.actionTarget = actionTarget;
    this.controlType = controlType;
    this.controlFactory = (ControlFactory<Object>)controlFactoriesByType.get(controlType);

    if(this.controlFactory == null) {
      throw new IllegalArgumentException("ControlType '" + controlType + "' is not supported for: " + actionTarget);
    }
  }

  public Node[] createNode(Object root) {
    return controlFactory.create(actionTarget, actionTarget.findDirectParentFromRoot(root));
  }

  public String getLabel() {
    return actionTarget.getLabel(controlType);
  }

  public String getPropertyName() {
    return actionTarget.getMemberName();
  }
}