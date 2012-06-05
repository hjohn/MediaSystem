package hs.mediasystem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Constants;

public final class ConfigUtil {

  /**
   * Creates a configuration for the framework. Therefore this method attempts to create
   * a temporary cache dir. If creation of the cache dir is successful, it will be added
   * to the configuration.
   *
   * @return
   */
  public static Map<String, String> createConfig() {
    final File cachedir = createCacheDir();

    String[] packages = new String[] {
      "com.google.gdata.client.youtube",
      "com.google.gdata.data",
      "com.google.gdata.data.extensions",
      "com.google.gdata.data.media.mediarss",
      "com.google.gdata.data.youtube",
      "com.google.gdata.util",
      "com.moviejukebox.thetvdb",
      "com.moviejukebox.thetvdb.model",
      "hs.mediasystem.db",
      "hs.mediasystem.enrich",
      "hs.mediasystem.framework",
      "hs.mediasystem.fs",
      "hs.mediasystem.persist",
      "hs.mediasystem.screens",
      "hs.mediasystem.screens.selectmedia",
      "hs.mediasystem.screens",
      "hs.mediasystem.util",
      "hs.mediasystem.util.ini",
      "javafx.beans.binding",
      "javafx.beans.property",
      "javafx.beans.value",
      "javafx.scene",
      "javafx.scene.image",
      "javax.inject",
      "net.sf.jtmdb",
      "org.json",
      "org.jsoup",
      "org.jsoup.nodes",
      "org.jsoup.select",
      "org.apache.felix.dm"
    };

    String packageString = "";

    for(String packageName : packages) {
      if(!packageString.isEmpty()) {
        packageString += ",";
      }

      packageString += packageName;
    }

    Map<String, String> configMap = new HashMap<>();
    // Tells the framework to export the extension package, making it accessible
    // for the other shape bundels
    configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
      //            "org.apache.felix.example.extenderbased.host.extension; version=1.0.0");
      packageString);

    // if we could create a cache dir, we use it. Otherwise the platform default will be used
    if (cachedir != null) {
      configMap.put(Constants.FRAMEWORK_STORAGE, cachedir.getAbsolutePath());
    }

    return configMap;
  }

  /**
   * Tries to create a temporay cache dir. If creation of the cache dir is successful,
   * it will be returned. If creation fails, null will be returned.
   *
   * @return a {@code File} object representing the cache dir
   */
  private static File createCacheDir()
  {
    final File cachedir;
    try
    {
      cachedir = File.createTempFile("felix.example.extenderbased", null);
      cachedir.delete();
      createShutdownHook(cachedir);
      return cachedir;
    }
    catch (IOException e)
    {
      // temp dir creation failed, return null
      return null;
    }
  }

  /**
   * Adds a shutdown hook to the runtime, that will make sure, that the cache dir will
   * be deleted after the application has been terminated.
   */
  private static void createShutdownHook(final File cachedir)
  {
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      @Override
      public void run()
      {
        deleteFileOrDir(cachedir);
      }
    });
  }


  /**
   * Utility method used to delete the profile directory when run as
   * a stand-alone application.
   * @param file The file to recursively delete.
   **/
  private static void deleteFileOrDir(File file)
  {
    if (file.isDirectory())
    {
      File[] childs = file.listFiles();
      for (File child : childs)
      {
        deleteFileOrDir(child);
      }
    }
    file.delete();
  }
}