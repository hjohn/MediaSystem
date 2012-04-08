package hs.mediasystem.screens.optiondialog;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;

public class NumericOption extends Option {
  private final Property<Number> property;
  private final double stepSize;
  private final double min;
  private final double max;

  public NumericOption(Property<Number> property, String description, double stepSize, double min, double max, StringBinding binding) {
    super(description);
    this.property = property;
    this.stepSize = stepSize;
    this.min = min;
    this.max = max;

    label.textProperty().bind(binding);
  }

  public NumericOption(final Property<Number> property, String description, double stepSize, double min, double max, final String format) {
    this(property, description, stepSize, min, max, new StringBinding() {
      {
        bind(property);
      }

      @Override
      protected String computeValue() {
        return String.format(format, property.getValue().doubleValue());
      }
    });
  }

  public NumericOption(String description, double stepSize, double min, double max, String format) {
    this(null, description, stepSize, min, max, format);
  }

  public NumericOption(String description, double stepSize, String format) {
    this(description, stepSize, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, format);
  }

  @Override
  public void left() {
    double value = property.getValue().doubleValue();

    value -= stepSize;

    if(value < min) {
      value = min;
    }

    property.setValue(value);
  }

  @Override
  public void right() {
    double value = property.getValue().doubleValue();

    value += stepSize;

    if(value > max) {
      value = max;
    }

    property.setValue(value);
  }
}