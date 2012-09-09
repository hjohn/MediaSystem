package hs.mediasystem.util;

import java.util.logging.ConsoleHandler;

public class SystemOutConsoleHandler extends ConsoleHandler {
  public SystemOutConsoleHandler() {
    setOutputStream(System.out);
  }
}
