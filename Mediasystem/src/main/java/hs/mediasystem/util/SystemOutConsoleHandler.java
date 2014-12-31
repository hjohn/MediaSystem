package hs.mediasystem.util;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;

public class SystemOutConsoleHandler extends ConsoleHandler {

  @Override
  protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
    super.setOutputStream(System.out);
  }
}
