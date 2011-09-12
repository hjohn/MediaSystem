package hs.mediasystem;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.Border;

public class GlowBorder implements Border {
  private final Color color;
  private final int thickness;
  private final float halfThickness;

  public GlowBorder(Color color, int thickness) {
    this.color = color;
    this.thickness = thickness;
    this.halfThickness = (float)thickness / 2;
  }
  
  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2d = (Graphics2D)g;

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    GradientPaint gp = new GradientPaint(75, 75, color, 145, 145, color.darker(), true);
    g2d.setPaint(gp);
    RoundRectangle2D.Float rect = new RoundRectangle2D.Float(x + halfThickness, y + halfThickness, width - thickness, height - thickness, 10, 10);
    
    g2d.setStroke(new BasicStroke(thickness - 0.5f));
    g2d.draw(rect);
  }

  @Override
  public Insets getBorderInsets(Component c) {
    return new Insets(thickness, thickness, thickness, thickness);
  }

  @Override
  public boolean isBorderOpaque() {
    return true;
  }
}
