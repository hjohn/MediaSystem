package hs.mediasystem.util;

import java.util.logging.StreamHandler;

public class SystemOutConsoleHandler extends StreamHandler {
  public SystemOutConsoleHandler() {
    setOutputStream(System.out);
  }
}
