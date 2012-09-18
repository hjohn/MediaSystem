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
      "com.sun.xml.internal.ws.api.message",  // sublight
      "hs.mediasystem.beans",
      "hs.mediasystem.dao",
      "hs.mediasystem.db",
      "hs.mediasystem.entity",
      "hs.mediasystem.framework",
      "hs.mediasystem.persist",
      "hs.mediasystem.framework.player",
      "hs.mediasystem.screens",
      "hs.mediasystem.screens.optiondialog",
      "hs.mediasystem.screens.selectmedia",
      "hs.mediasystem.util",
      "hs.mediasystem.util.ini",
      "hs.subtitle",
      "javafx.application",
      "javafx.beans",
      "javafx.beans.binding",
      "javafx.beans.property",
      "javafx.beans.value",
      "javafx.collections",
      "javafx.geometry",
      "javafx.scene.canvas",
      "javafx.scene.control.cell",
      "javafx.scene.paint",
      "javafx.event",
      "javafx.scene",
      "javafx.scene.control",
      "javafx.scene.image",
      "javafx.scene.input",
      "javafx.scene.layout",
      "javafx.stage",
      "javafx.util",
      "javax.inject",
      "javax.persistence",
      "javax.servlet",
      "javax.servlet.http",
      "org.jsoup",
      "org.jsoup.nodes",
      "org.jsoup.select",
      "org.apache.felix.dm",

      // Java 8 exports...
      "javax.management",
      "javax.xml.datatype",
      "javax.xml.parsers",
      "javax.annotation",
      "javax.imageio",
      "org.xml.sax",
      "javax.jws",
      "org.xml.sax.helpers",
      "javax.swing",
      "javax.crypto",
      "javax.xml.transform.stream",
      "javax.xml.transform",
      "javax.crypto.spec",
      "javax.naming",
      "javax.swing.event",
      "javax.xml.bind.annotation",
      "javax.swing.filechooser",
      "javax.net",
      "javax.xml.transform.dom",
      "javax.xml.namespace",
      "javax.net.ssl",
      "javax.swing.border",
      "javax.xml.ws",
      "javax.security.auth.callback",
      "javax.xml.ws",
      "javax.swing.table",
      "javax.security.auth.x500",
      "javax.swing.text",
      "org.w3c.dom",
      "javax.security.sasl",
      "javax.swing.tree",
      "javax.xml.validation"
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

    configMap.put(Constants.FRAMEWORK_BOOTDELEGATION, "com.sun.xml.internal.ws.*");
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