package hs.mediasystem.config;

import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Resources {
  public static ResourceBundle resourceBundle = new ListResourceBundle() {
    @Override
    protected Object[][] getContents() {
      return new Object[][] {
      };
    }
  };

  public static String getResource(String propertyName, String key) {
    try {
      return resourceBundle.getString(propertyName + ":" + key);
    }
    catch(MissingResourceException e) {
      System.out.println("[WARN] Resource missing for key: " + propertyName + ":" + key);
      return "<" + propertyName.substring(propertyName.lastIndexOf(".") + 1) + "." + key + ">";
    }
  }
}
