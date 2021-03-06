package hs.mediasystem.ext.subtitle.opensubtitles;

import static java.util.Collections.singleton;
import hs.mediasystem.ext.subtitle.opensubtitles.OpenSubtitlesSubtitleDescriptor.Property;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.util.Base64;

public class OpenSubtitlesXmlRpc {

  private final String useragent;

  private String token;

  public OpenSubtitlesXmlRpc(String useragent) {
    this.useragent = useragent;
  }

  /**
   * Login as anonymous user.
   */
  public void loginAnonymous() throws XmlRpcFault {
    login("", "", "en");
  }

  /**
   * This will login user. This method should be called always when starting talking with server.
   *
   * @param username username (blank for anonymous user)
   * @param password password (blank for anonymous user)
   * @param language ISO639 2-letter codes as language and later communication will be done in this language if
   *          applicable (error codes and so on).
   */
  public synchronized void login(String username, String password, String language) throws XmlRpcFault {
    Map<?, ?> response = invoke("LogIn", username, password, language, useragent);

    // set session token
    token = response.get("token").toString();
  }

  /**
   * This will logout user (ends session id). Call this function is before closing the client program.
   */
  public synchronized void logout() {
    try {
      invoke("LogOut", token);
    }
    catch(XmlRpcFault e) {
      // anonymous users will always get an 401 Unauthorized when trying to
      // logout, so we ignore the status of the logout response
    }
    finally {
      token = null;
    }
  }

  public boolean isLoggedOn() {
    return token != null;
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getServerInfo() throws XmlRpcFault {
    return (Map<String, String>) invoke("ServerInfo", token);
  }

  public List<OpenSubtitlesSubtitleDescriptor> searchSubtitles(int imdbid, String... sublanguageids) throws XmlRpcFault {
    return searchSubtitles(singleton(Query.forImdbId(imdbid, sublanguageids)));
  }

  public List<OpenSubtitlesSubtitleDescriptor> searchSubtitles(Query query) throws XmlRpcFault {
    List<Query> queries = new ArrayList<>();

    queries.add(query);

    return searchSubtitles(queries);
  }

  @SuppressWarnings("unchecked")
  public List<OpenSubtitlesSubtitleDescriptor> searchSubtitles(Collection<Query> queryList) throws XmlRpcFault {
    List<OpenSubtitlesSubtitleDescriptor> subtitles = new ArrayList<>();
    Map<?, ?> response = invoke("SearchSubtitles", token, queryList);

    try {
      List<Map<String, String>> subtitleData = (List<Map<String, String>>)response.get("data");

      for(Map<String, String> propertyMap : subtitleData) {
        subtitles.add(new OpenSubtitlesSubtitleDescriptor(Property.asEnumMap(propertyMap)));
      }
    }
    catch(ClassCastException e) {
      // no subtitle have been found
    }

    return subtitles;
  }

  @SuppressWarnings("unchecked")
  public List<Movie> searchMoviesOnIMDB(String query) throws XmlRpcFault {
    Map<?, ?> response = invoke("SearchMoviesOnIMDB", token, query);

    List<Map<String, String>> movieData = (List<Map<String, String>>) response.get("data");
    List<Movie> movies = new ArrayList<>();

    // title pattern
    Pattern pattern = Pattern.compile("(.+)[(](\\d{4})([/]I+)?[)]");

    for(Map<String, String> movie : movieData) {
      try {
        String imdbid = movie.get("id");
        if(!imdbid.matches("\\d{1,7}")) {
          throw new IllegalArgumentException("Illegal IMDb movie ID");
        }

        // match movie name and movie year from search result
        Matcher matcher = pattern.matcher(movie.get("title"));
        if(!matcher.find()) {
          throw new IllegalArgumentException("Illegal title");
        }

        String name = matcher.group(1).replaceAll("\"", "").trim();
        int year = Integer.parseInt(matcher.group(2));

        movies.add(new Movie(name, year, Integer.parseInt(imdbid)));
      }
      catch(RuntimeException e) {
        Logger.getLogger(OpenSubtitlesXmlRpc.class.getName()).log(Level.INFO, String.format("Ignore movie %s: %s", movie, e.getMessage()));
      }
    }

    return movies;
  }

  @SuppressWarnings("unchecked")
  public TryUploadResponse tryUploadSubtitles(SubFile... subtitles) throws XmlRpcFault {
    Map<String, SubFile> struct = new HashMap<>();

    // put cd1, cd2, ...
    for(SubFile cd : subtitles) {
      struct.put(String.format("cd%d", struct.size() + 1), cd);
    }

    Map<?, ?> response = invoke("TryUploadSubtitles", token, struct);

    boolean uploadRequired = response.get("alreadyindb").equals("0");
    Map<String, String> subtitleData = (Map<String, String>) response.get("data");

    return new TryUploadResponse(uploadRequired, Property.asEnumMap(subtitleData));
  }

  public URI uploadSubtitles(BaseInfo baseInfo, SubFile... subtitles) throws XmlRpcFault {
    Map<String, Object> struct = new HashMap<>();

    // put cd1, cd2, ...
    for(SubFile cd : subtitles) {
      struct.put(String.format("cd%d", struct.size() + 1), cd);
    }

    // put baseinfo
    struct.put("baseinfo", baseInfo);

    Map<?, ?> response = invoke("UploadSubtitles", token, struct);

    // subtitle link
    return URI.create(response.get("data").toString());
  }

  @SuppressWarnings("unchecked")
  public List<String> detectLanguage(byte[] data) throws XmlRpcFault {
    // compress and base64 encode
    String parameter = encodeData(data);

    Map<String, Map<String, String>> response = (Map<String, Map<String, String>>) invoke("DetectLanguage", token, singleton(parameter));
    List<String> languages = new ArrayList<>(2);

    if(response.containsKey("data")) {
      languages.addAll(response.get("data").values());
    }

    return languages;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Integer> checkSubHash(Collection<String> hashes) throws XmlRpcFault {
    Map<?, ?> response = invoke("CheckSubHash", token, hashes);

    Map<String, ?> subHashData = (Map<String, ?>) response.get("data");
    Map<String, Integer> subHashMap = new HashMap<>();

    for(Entry<String, ?> entry : subHashData.entrySet()) {
      // non-existing subtitles are represented as Integer 0, not String "0"
      subHashMap.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
    }

    return subHashMap;
  }

  @SuppressWarnings("unchecked")
  public Map<String, Movie> checkMovieHash(Collection<String> hashes) throws XmlRpcFault {
    Map<?, ?> response = invoke("CheckMovieHash", token, hashes);

    Map<String, ?> movieHashData = (Map<String, ?>) response.get("data");
    Map<String, Movie> movieHashMap = new HashMap<>();

    for(Entry<String, ?> entry : movieHashData.entrySet()) {
      // empty associative arrays are deserialized as array, not as map
      if(entry.getValue() instanceof Map) {
        Map<String, String> info = (Map<String, String>) entry.getValue();

        String hash = info.get("MovieHash");
        String name = info.get("MovieName");
        int year = Integer.parseInt(info.get("MovieYear"));
        int imdb = Integer.parseInt(info.get("MovieImdbID"));

        movieHashMap.put(hash, new Movie(name, year, imdb));
      }
    }

    return movieHashMap;
  }

  public Map<String, String> getSubLanguages() throws XmlRpcFault {
    return getSubLanguages("en");
  }

  @SuppressWarnings("unchecked")
  public Movie getIMDBMovieDetails(int imdbid) throws XmlRpcFault {
    Map<?, ?> response = invoke("GetIMDBMovieDetails", token, imdbid);

    try {
      Map<String, String> data = (Map<String, String>) response.get("data");

      String name = data.get("title");
      int year = Integer.parseInt(data.get("year"));

      return new Movie(name, year, imdbid);
    }
    catch(RuntimeException e) {
      // ignore, invalid response
      Logger.getLogger(getClass().getName()).log(Level.WARNING, String.format("Failed to lookup movie by imdbid %s: %s", imdbid, e.getMessage()));
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getSubLanguages(String languageCode) throws XmlRpcFault {
    Map<String, List<Map<String, String>>> response = (Map<String, List<Map<String, String>>>) invoke("GetSubLanguages", languageCode);

    Map<String, String> subLanguageMap = new HashMap<>();

    for(Map<String, String> language : response.get("data")) {
      subLanguageMap.put(language.get("SubLanguageID"), language.get("LanguageName"));
    }

    return subLanguageMap;
  }

  public void noOperation() throws XmlRpcFault {
    invoke("NoOperation", token);
  }

  protected Map<?, ?> invoke(String method, Object... arguments) throws XmlRpcFault {
    try {
      XmlRpcClient rpc = new XmlRpcClient(getXmlRpcUrl(), false);

      Map<?, ?> response = (Map<?, ?>) rpc.invoke(method, arguments);
      checkResponse(response);

      return response;
    }
    catch(XmlRpcFault e) {
      // invalidate session token if session has expired
      if(e.getErrorCode() == 406) {
        token = null;
      }

      // rethrow exception
      throw e;
    }
  }

  protected static URL getXmlRpcUrl() {
    try {
      return new URL("http://api.opensubtitles.org/xml-rpc");
    }
    catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  protected static String encodeData(byte[] data) {
    try(DataInputStream stream = new DataInputStream(new DeflaterInputStream(new ByteArrayInputStream(data)))) {
      byte[] buffer = new byte[data.length];

      stream.readFully(buffer);

      return new String(Base64.encode(buffer));
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Check whether status is OK or not.
   *
   * @param status status code and message (e.g. 200 OK, 401 Unauthorized, ...)
   * @throws XmlRpcFault thrown if status code is not OK
   */
  protected static void checkResponse(Map<?, ?> response) throws XmlRpcFault {
    String status = (String) response.get("status");

    // if there is no status at all, assume everything was OK
    if(status == null || status.equals("200 OK")) {
      return;
    }

    try {
      throw new XmlRpcFault(new Scanner(status).nextInt(), status);
    }
    catch(NoSuchElementException e) {
      throw new XmlRpcException("Illegal status code: " + status, e);
    }
  }

  public static final class Query extends HashMap<String, Object> {

    private Query(String imdbid, String... sublanguageids) {
      put("imdbid", imdbid);
      put("sublanguageid", join(sublanguageids, ","));
    }

    private Query(String moviehash, String moviebytesize, String... sublanguageids) {
      put("moviehash", moviehash);
      put("moviebytesize", moviebytesize);
      put("sublanguageid", join(sublanguageids, ","));
    }

    public static Query forHash(String moviehash, long moviebytesize, String... sublanguageids) {
      return new Query(moviehash, Long.toString(moviebytesize), sublanguageids);
    }

    public static Query forImdbId(int imdbid, String... sublanguageids) {
      return new Query(Integer.toString(imdbid), sublanguageids);
    }

    public Query(String imdbid, String name, Integer season, Integer episode, String moviehash, Long moviebytesize, String... sublanguageids) {
      if(imdbid != null) {
        put("imdbid", imdbid);
      }
      if(name != null) {
        put("query", name);
      }
      if(season != null) {
        put("season", season.toString());
      }
      if(episode != null) {
        put("episode", episode.toString());
      }
      if(moviehash != null) {
        put("moviehash", moviehash);
      }
      if(moviebytesize != null) {
        put("moviebytesize", moviebytesize.toString());
      }
      put("sublanguageid", join(sublanguageids, ","));
    }
  }

  public static final class BaseInfo extends HashMap<String, Object> {

    public void setIDMovieImdb(int imdb) {
      put("idmovieimdb", Integer.toString(imdb));
    }

    public void setSubLanguageID(String sublanguageid) {
      put("sublanguageid", sublanguageid);
    }

    public void setMovieReleaseName(String moviereleasename) {
      put("moviereleasename", moviereleasename);
    }

    public void setMovieAka(String movieaka) {
      put("movieaka", movieaka);
    }

    public void setSubAuthorComment(String subauthorcomment) {
      put("subauthorcomment", subauthorcomment);
    }
  }

  public static final class SubFile extends HashMap<String, Object> {

    public void setSubHash(String subhash) {
      put("subhash", subhash);
    }

    public void setSubFileName(String subfilename) {
      put("subfilename", subfilename);
    }

    public void setMovieHash(String moviehash) {
      put("moviehash", moviehash);
    }

    public void setMovieByteSize(long moviebytesize) {
      put("moviebytesize", Long.toString(moviebytesize));
    }

    public void setMovieTimeMS(int movietimems) {
      put("movietimems", movietimems);
    }

    public void setMovieFrames(int movieframes) {
      put("movieframes", movieframes);
    }

    public void setMovieFPS(double moviefps) {
      put("moviefps", moviefps);
    }

    public void setMovieFileName(String moviefilename) {
      put("moviefilename", moviefilename);
    }

    public void setSubContent(byte[] data) {
      put("subcontent", encodeData(data));
    }
  }

  public static final class TryUploadResponse {

    private final boolean uploadRequired;

    private final Map<Property, String> subtitleData;

    TryUploadResponse(boolean uploadRequired, Map<Property, String> subtitleData) {
      this.uploadRequired = uploadRequired;
      this.subtitleData = subtitleData;
    }

    public boolean isUploadRequired() {
      return uploadRequired;
    }

    public Map<Property, String> getSubtitleData() {
      return subtitleData;
    }
  }

  public static String join(Object[] values, CharSequence delimiter) {
    return join(Arrays.asList(values), delimiter);
  }

  public static String join(Iterable<?> values, CharSequence delimiter) {
    StringBuilder sb = new StringBuilder();

    for(Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
      Object value = iterator.next();
      if(!isEmptyValue(value)) {
        if(sb.length() > 0) {
          sb.append(delimiter);
        }

        sb.append(value);
      }
    }

    return sb.toString();
  }

  public static boolean isEmptyValue(Object object) {
    return object == null || object.toString().length() == 0;
  }
}