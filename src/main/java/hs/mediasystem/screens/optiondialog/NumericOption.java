package hs.mediasystem.screens.optiondialog;

import hs.mediasystem.util.StringConverter;
import javafx.beans.value.WritableNumberValue;

public class NumericOption extends Option {
  private final WritableNumberValue property;
  private final StringConverter<Number> converter;
  private final double stepSize;
  private final double min;
  private final double max;

  private double value;

  public NumericOption(WritableNumberValue property, String description, double stepSize, double min, double max, StringConverter<Number> converter) {
    super(description);
    this.property = property;
    this.converter = converter;
    this.stepSize = stepSize;
    this.min = min;
    this.max = max;

    if(property != null) {
      value = property.getValue().doubleValue();
    }

    updateControl();
  }

  public NumericOption(WritableNumberValue property, String description, double stepSize, double min, double max, final String format) {
    this(property, description, stepSize, min, max, new StringConverter<Number>() {
      @Override
      public String toString(Number object) {
        return String.format(format, object);
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
    label.setText(converter.toString(value));
  }
}