package hs.mediasystem.db;

public class URLImageSource implements Source<byte[]> {
  private final String url;

  /**
   * Constructs a new instance of this class.
   *
   * @param url a URL where the image can be fetched from, or <code>null</code> if the image should be fetched only from the database
   */
  public URLImageSource(String url) {
    this.url = url;
  }

  @Override
  public byte[] get() {
    System.out.println("[FINE] URLImageSource.get() - Downloading '" + url + "'");
    return Downloader.tryReadURL(url);
  }
}