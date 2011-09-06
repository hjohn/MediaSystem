package hs.mediasystem.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Downloader {
  public static final byte[] readURL(URL url) {
    ByteArrayOutputStream bais = new ByteArrayOutputStream();
    InputStream is = null;

    try {
      is = url.openStream();
      byte[] byteChunk = new byte[4096];
      int n;

      while((n = is.read(byteChunk)) > 0) {
        bais.write(byteChunk, 0, n);
      }
      
      return bais.toByteArray();
    }
    catch(IOException e) {
      System.err.println("Error reading url: " + url + ": " + e);
      return null;
    }
    finally {
      if (is != null) { 
        try {
          is.close();
        }
        catch(IOException e) {
          // don't care
        } 
      }
    }
  }
}
