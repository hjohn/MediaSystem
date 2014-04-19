package hs.mediasystem.dao;

import java.net.URL;

import hs.mediasystem.util.io.RuntimeIOException;
import hs.mediasystem.util.io.URLs;

public class URLImageSource implements Source<byte[]> {
  private final URL url;

  /**
   * Constructs a new instance of this class.
   *
   * @param url a URL where the image can be fetched from
   */
  public URLImageSource(URL url) {
    if(url == null) {
      throw new IllegalArgumentException("parameter 'url' cannot be null");
    }
    
    this.url = url;
  }

  @Override
  public byte[] get() {
    System.out.println("[FINE] " + getClass().getName() + "::get() - Downloading '" + url + "'");

    try {
      return URLs.readAllBytes(url);
    }
    catch(RuntimeIOException e) {
      return null;
    }
  }

  @Override
  public boolean isLocal() {
    return false;
  }
}