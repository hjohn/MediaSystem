package hs.subtitle.opensub;

import hs.subtitle.SearchResult;
import hs.subtitle.SubtitleDescriptor;
import hs.subtitle.Timer;
import hs.subtitle.opensub.OpenSubtitlesXmlRpc.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

/**
 * SubtitleClient for OpenSubtitles.
 */
public class OpenSubtitlesClient {
  private final OpenSubtitlesXmlRpc xmlrpc;

  public OpenSubtitlesClient(String userAgent) {
    this.xmlrpc = new OpenSubtitlesXmlRpc(userAgent);
  }

  public List<SearchResult> search(String query) throws Exception {
    // require login
    login();

    try {
      // search for movies / series
      List<Movie> resultSet = xmlrpc.searchMoviesOnIMDB(query);
      return Arrays.asList(resultSet.toArray(new SearchResult[0]));
    }
    catch(ClassCastException e) {
      // unexpected xmlrpc responses (e.g. error messages instead of results) will trigger this
      throw new XmlRpcException("Illegal XMLRPC response on searchMoviesOnIMDB");
    }
  }

  public List<SubtitleDescriptor> getSubtitleList(SearchResult searchResult, String languageName) throws XmlRpcFault {
    // singleton array with or empty array
    int imdbid = ((Movie) searchResult).getImdbId();
    String[] languageFilter = languageName != null ? new String[] {getSubLanguageID(languageName)} : new String[0];

    // require login
    login();

    // get subtitle list
    SubtitleDescriptor[] subtitles = xmlrpc.searchSubtitles(imdbid, languageFilter).toArray(new SubtitleDescriptor[0]);

    return Arrays.asList(subtitles);
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

  public Locale detectLanguage(byte[] data) throws Exception {
    // require login
    login();

    // detect language
    List<String> languages = xmlrpc.detectLanguage(data);

    // return first language
    return languages.size() > 0 ? new Locale(languages.get(0)) : null;
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

  protected final Timer logoutTimer = new Timer() {
    @Override
    public void run() {
      logout();
    }
  };

  private Map<String, String> cachedLanguages;

  /**
   * SubLanguageID by English language name
   */
  protected synchronized Map<String, String> getSubLanguageMap() throws XmlRpcFault {

    if(cachedLanguages == null) {
      Map<String, String> cachedLanguages = new HashMap<>();

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

  protected String getLanguageName(String subLanguageID) throws Exception {
    for(Entry<String, String> it : getSubLanguageMap().entrySet()) {
      if(it.getValue().equals(subLanguageID.toLowerCase()))
        return it.getKey();
    }

    return null;
  }

}