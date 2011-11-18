package hs.mediasystem.framework.db;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class Handler extends URLStreamHandler {

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    return null;
  }
  
  
  private static class MyURLConnection extends URLConnection {
    protected MyURLConnection(URL url) {
      super(url);
    }

    @Override
    public void connect() throws IOException {
      throw new UnsupportedOperationException("Method not implemented");
    }
  }
}
