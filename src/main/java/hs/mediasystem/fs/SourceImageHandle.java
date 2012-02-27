package hs.mediasystem.fs;

import hs.mediasystem.db.Source;
import hs.mediasystem.util.ImageHandle;

public class SourceImageHandle implements ImageHandle {
  private final Source<byte[]> source;
  private final String key;

  public SourceImageHandle(Source<byte[]> source, String key) {
    assert key != null;

    this.source = source;
    this.key = key;
  }

  @Override
  public byte[] getImageData() {
    return source.get();
  }

  @Override
  public String getKey() {
    return key;
  }
}
