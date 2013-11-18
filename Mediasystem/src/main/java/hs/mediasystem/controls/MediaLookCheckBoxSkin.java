package hs.mediasystem.controls;

import javafx.geometry.HPos;
import javafx.geometry.NodeOrientation;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;

import com.sun.javafx.scene.control.skin.LabeledSkinBase;

/*
 * This class is a minimal reimplementation of the likely named class of the JDK as it is the only way to change the
 * behavior of the relevant UI controls.
 */

public class MediaLookCheckBoxSkin extends LabeledSkinBase<CheckBox, MediaLookButtonBehavior<CheckBox>> {
  private final StackPane box = new StackPane();
  private final StackPane innerbox;

  public MediaLookCheckBoxSkin(CheckBox checkbox) {
    super(checkbox, new MediaLookButtonBehavior<>(checkbox));

    box.getStyleClass().setAll("box");
    innerbox = new StackPane();
    innerbox.getStyleClass().setAll("mark");
    innerbox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
    box.getChildren().add(innerbox);
    updateChildren();
  }

  @Override
  protected void updateChildren() {
    super.updateChildren();
    if(box != null) {
      getChildren().add(box);
    }
  }

  @Override
  protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
    return super.computeMinWidth(height, topInset, rightInset, bottomInset, leftInset) + snapSize(box.minWidth(-1));
  }

  @Override
  protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
    return Math.max(super.computeMinHeight(width - box.minWidth(-1), topInset, rightInset, bottomInset, leftInset), topInset + box.minHeight(-1) + bottomInset);
  }

  @Override
  protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
    return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset) + snapSize(box.prefWidth(-1));
  }

  @Override
  protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
    return Math.max(super.computePrefHeight(width - box.prefWidth(-1), topInset, rightInset, bottomInset, leftInset), topInset + box.prefHeight(-1) + bottomInset);
  }

  @Override
  protected void layoutChildren(final double x, final double y, final double w, final double h) {
    final CheckBox checkBox = getSkinnable();
    final double boxWidth = snapSize(box.prefWidth(-1));
    final double boxHeight = snapSize(box.prefHeight(-1));
    final double computeWidth = Math.max(checkBox.prefWidth(-1), checkBox.minWidth(-1));
    final double labelWidth = Math.min(computeWidth - boxWidth, w - snapSize(boxWidth));
    final double labelHeight = Math.min(checkBox.prefHeight(labelWidth), h);
    final double maxHeight = Math.max(boxHeight, labelHeight);
    final double xOffset = computeXOffset(w, labelWidth + boxWidth, checkBox.getAlignment().getHpos()) + x;
    final double yOffset = computeYOffset(h, maxHeight, checkBox.getAlignment().getVpos()) + x;

    layoutLabelInArea(xOffset + boxWidth, yOffset, labelWidth, maxHeight, checkBox.getAlignment());
    box.resize(boxWidth, boxHeight);
    positionInArea(box, xOffset, yOffset, boxWidth, maxHeight, 0, checkBox.getAlignment().getHpos(), checkBox.getAlignment().getVpos());
  }

  static double computeXOffset(double width, double contentWidth, HPos hpos) {
    switch(hpos) {
    case LEFT:
      return 0;
    case CENTER:
      return (width - contentWidth) / 2;
    case RIGHT:
      return width - contentWidth;
    }
    return 0;
  }

  static double computeYOffset(double height, double contentHeight, VPos vpos) {
    switch(vpos) {
    case TOP:
      return 0;
    case CENTER:
      return (height - contentHeight) / 2;
    case BOTTOM:
      return height - contentHeight;
    default:
      return 0;
    }
  }
}
