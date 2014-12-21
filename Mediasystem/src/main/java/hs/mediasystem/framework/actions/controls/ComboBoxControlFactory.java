package hs.mediasystem.framework.actions.controls;

import hs.mediasystem.framework.actions.ActionTarget;
import hs.mediasystem.framework.actions.Expose;
import hs.mediasystem.framework.actions.StringConvertingCell;
import hs.mediasystem.util.StringConverter;

import java.lang.reflect.Field;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

public class ComboBoxControlFactory implements ControlFactory<Object> {

  @Override
  public Node[] create(ActionTarget actionTarget, Object parent) {
    try {
      Expose expose = actionTarget.getExposedMember().getMember().getAnnotation(Expose.class);

      Field field = parent.getClass().getDeclaredField(expose.values());

      @SuppressWarnings("unchecked")
      ComboBox<Object> comboBox = new ComboBox<>((ObservableList<Object>)field.get(parent));

      @SuppressWarnings("unchecked")
      StringConverter<Object> stringConverter = (StringConverter<Object>)expose.stringConverter().newInstance();

      comboBox.setCellFactory(listView -> new StringConvertingCell<>(stringConverter));
      comboBox.setButtonCell(new StringConvertingCell<>(stringConverter));
      comboBox.valueProperty().bindBidirectional(actionTarget.getProperty(parent));

      return new Node[] {comboBox};
    }
    catch(Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
