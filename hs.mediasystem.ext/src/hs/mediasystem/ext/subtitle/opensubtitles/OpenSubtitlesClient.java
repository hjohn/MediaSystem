package hs.mediasystem.ext.subtitle.opensubtitles;

import hs.mediasystem.ext.subtitle.opensubtitles.OpenSubtitlesXmlRpc.Query;
import hs.subtitle.SubtitleDescriptor;
import hs.subtitle.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import redstone.xmlrpc.XmlRpcFault;

/**
 * SubtitleClient for OpenSubtitles.
 */
public class OpenSubtitlesClient {
  private final OpenSubtitlesXmlRpc xmlrpc;

  public OpenSubtitlesClient(String userAgent) {
    this.xmlrpc = new OpenSubtitlesXmlRpc(userAgent);
  }

  public List<? extends SubtitleDescriptor> getAllSubtitleLists(String imdbId, String name, Integer season, Integer episode, String movieHash, Long fileLength, String languageName) throws XmlRpcFault {
    String searchImdbId = imdbId;

    if(imdbId != null && imdbId.startsWith("tt")) {
      searchImdbId = imdbId.substring(2);
    }

    // require login
    login();

    List<OpenSubtitlesSubtitleDescriptor> foundSubtitles = new ArrayList<>();

    if(movieHash != null && fileLength != null) {
      foundSubtitles.addAll(xmlrpc.searchSubtitles(new Query(null, null, null, null, movieHash, fileLength, languageName)));
    }
    if(searchImdbId != null) {
      foundSubtitles.addAll(xmlrpc.searchSubtitles(new Query(searchImdbId, null, null, null, null, null, languageName)));
    }
    foundSubtitles.addAll(xmlrpc.searchSubtitles(new Query(null, name, season, episode, null, null, languageName)));

    return foundSubtitles;
  }

  public List<? extends SubtitleDescriptor> getSubtitleList(String imdbId, String name, Integer season, Integer episode, String languageName) throws XmlRpcFault {
    String searchImdbId = imdbId;

    if(imdbId != null && imdbId.startsWith("tt")) {
      searchImdbId = imdbId.substring(2);
    }

    // require login
    login();

    return xmlrpc.searchSubtitles(new Query(searchImdbId, name, season, episode, null, null, languageName));
  }

  public Locale detectLanguage(byte[] data) throws XmlRpcFault {
    // require login
    login();

    // detect language
    List<String> languages = xmlrpc.detectLanguage(data);

    // return first language
    return !languages.isEmpty() ? new Locale(languages.get(0)) : null;
  }

  protected synchronized void login() throws XmlRpcFault {
    if(!xmlrpc.isLoggedOn()) {
      xmlrpc.loginAnonymous();
    }

    logoutTimer.set(10, TimeUnit.MINUTES, true);
  }

  protected synchronized void logout() {
    if(xmlrpc.isLoggedOn()) {
      try {
        xmlrpc.logout();
      }
      catch(Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Logout failed", e);
      }
    }

    logoutTimer.cancel();
  }

  private final Timer logoutTimer = new Timer() {
    @Override
    public void run() {
      logout();
    }
  };

  private Map<String, String> cachedLanguages;

  /**
   * SubLanguageID by English language name.
   */
  protected synchronized Map<String, String> getSubLanguageMap() throws XmlRpcFault {
    if(cachedLanguages == null) {
      cachedLanguages = new HashMap<>();

      // fetch language data
      for(Entry<String, String> entry : xmlrpc.getSubLanguages().entrySet()) {
        // map id by name
        cachedLanguages.put(entry.getValue().toLowerCase(), entry.getKey().toLowerCase());
      }

      // some additional special handling
      cachedLanguages.put("brazilian", "pob");
    }

    return cachedLanguages;
  }

  protected String getSubLanguageID(String languageName) throws XmlRpcFault {
    Map<String, String> subLanguageMap = getSubLanguageMap();
    String key = languageName.toLowerCase();

    if(!subLanguageMap.containsKey(key)) {
      throw new IllegalArgumentException(String.format("SubLanguageID for '%s' not found", key));
    }

    return subLanguageMap.get(key);
  }
}