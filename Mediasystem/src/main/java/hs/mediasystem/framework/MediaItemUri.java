package hs.mediasystem.framework;

import hs.mediasystem.enrich.Parameters.WrappedValue;

public class MediaItemUri implements WrappedValue<String> {
  private final String uri;

  public MediaItemUri(String uri) {
    this.uri = uri;
  }

  @Override
  public String get() {
    return uri;
  }
}
