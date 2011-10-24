package hs.mediasystem;

import java.awt.GraphicsDevice;

public interface ControllerFactory {
  public Controller create(GraphicsDevice device);
}
