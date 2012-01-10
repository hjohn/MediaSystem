package hs.mediasystem.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Downloader {
  public static final byte[] readURL(URL url) {
    try {
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
    catch(IOException e) {
      System.out.println("[WARN] Error reading url: " + url + ": " + e);
      return null;
    }
  }
}
