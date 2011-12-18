package hs.mediasystem;

import hs.mediasystem.util.ini.Ini;

import java.awt.GraphicsDevice;

public interface ControllerFactory {
  ProgramController create(Ini ini, GraphicsDevice device);
}
