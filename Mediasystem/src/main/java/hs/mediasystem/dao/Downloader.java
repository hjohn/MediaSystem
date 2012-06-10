package hs.mediasystem.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Downloader {

  public static final byte[] tryReadURL(String urlString) {
    try {
      return readURL(urlString);
    }
    catch(IOException e) {
      return null;
    }
  }

  public static final byte[] readURL(String urlString) throws IOException {
    URL url = new URL(urlString);

    try(InputStream is = url.openStream();
        ByteArrayOutputStream bais = new ByteArrayOutputStream()) {
      byte[] byteChunk = new byte[4096];
      int n;

      while((n = is.read(byteChunk)) > 0) {
        bais.write(byteChunk, 0, n);
      }

      return bais.toByteArray();
    }
  }
}
