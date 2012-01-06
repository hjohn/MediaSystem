package hs.mediasystem.screens;

import javafx.beans.value.WritableNumberValue;

public class NumericOption extends Option {
  private final WritableNumberValue property;
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
    label.setText(String.format(format, value));
  }
}