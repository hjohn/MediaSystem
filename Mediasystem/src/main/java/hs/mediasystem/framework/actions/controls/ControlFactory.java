package hs.mediasystem.framework.actions.controls;

import hs.mediasystem.framework.actions.ActionTarget;
import javafx.scene.Node;

public interface ControlFactory<T> {
  Node[] create(ActionTarget actionTarget, Object parent);
}
