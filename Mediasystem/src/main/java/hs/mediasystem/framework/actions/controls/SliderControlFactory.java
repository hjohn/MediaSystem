package hs.mediasystem.framework.actions.controls;

import hs.mediasystem.framework.actions.ActionTarget;
import hs.mediasystem.framework.actions.Range;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;

public class SliderControlFactory implements ControlFactory<Number> {

  @Override
  public Node[] create(ActionTarget actionTarget, Object parent) {
    Property<Number> property = actionTarget.getProperty(parent);

    Range range = actionTarget.getExposedMember().getMember().getAnnotation(Range.class);
    double min = 0;
    double max = 100;
    double step = 1;

    if(range != null) {
      min = range.min();
      max = range.max();
      step = range.step();
    }

    Slider slider = new Slider(min, max, 0);

    slider.valueProperty().bindBidirectional(property);
    slider.setBlockIncrement(step);

    Label label = new Label();

    label.setMinWidth(50);
    label.setTextAlignment(TextAlignment.LEFT);
    label.textProperty().bindBidirectional(property, new StringConverter<Number>() {
      @Override
      public String toString(Number object) {
        return object.toString();
      }

      @Override
      public Number fromString(String string) {
        throw new UnsupportedOperationException();
      }
    });

    return new Node[] {slider, label};
  }
}
