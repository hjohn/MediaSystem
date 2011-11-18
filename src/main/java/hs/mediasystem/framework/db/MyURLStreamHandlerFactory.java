package hs.mediasystem.framework.db;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class MyURLStreamHandlerFactory implements URLStreamHandlerFactory {

  @Override
  public URLStreamHandler createURLStreamHandler(String protocol) {
    if(protocol.equals("db")) {
      return new Handler();
    }
    
    return null;
  }

}
