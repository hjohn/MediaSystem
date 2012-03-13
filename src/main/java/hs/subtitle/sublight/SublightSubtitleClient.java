package hs.subtitle.sublight;

import hs.subtitle.SearchResult;
import hs.subtitle.SubtitleDescriptor;
import hs.subtitle.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import net.sublight.webservice.ArrayOfGenre;
import net.sublight.webservice.ArrayOfIMDB;
import net.sublight.webservice.ArrayOfRelease;
import net.sublight.webservice.ArrayOfString;
import net.sublight.webservice.ArrayOfSubtitle;
import net.sublight.webservice.ArrayOfSubtitleLanguage;
import net.sublight.webservice.ClientInfo;
import net.sublight.webservice.Genre;
import net.sublight.webservice.IMDB;
import net.sublight.webservice.Release;
import net.sublight.webservice.Sublight;
import net.sublight.webservice.SublightSoap;
import net.sublight.webservice.Subtitle;
import net.sublight.webservice.SubtitleLanguage;

public class SublightSubtitleClient {
  private static final String IID = "42cc1701-3752-49e2-a148-332960073452";

  private final ClientInfo clientInfo = new ClientInfo();

  private SublightSoap webservice;
  private String session;

  public SublightSubtitleClient(String clientIdentity, String apikey) {
    clientInfo.setClientId(clientIdentity);
    clientInfo.setApiKey(apikey);
  }

  public List<SearchResult> search(String query) {
    // require login
    login();

    Holder<ArrayOfIMDB> response = new Holder<>();
    Holder<String> error = new Holder<>();

    webservice.findIMDB(query, null, null, response, error);

    // abort if something went wrong
    checkError(error);

    List<SearchResult> results = new ArrayList<>();

    if(response.value != null) {
      for(IMDB imdb : response.value.getIMDB()) {
        // remove classifier (e.g. tt0436992 -> 0436992)
        int id = Integer.parseInt(imdb.getId().substring(2));

        results.add(new MovieDescriptor(imdb.getTitle(), imdb.getYear(), id));
      }
    }

    return results;
  }

  public List<SubtitleDescriptor> getSubtitleList(SearchResult searchResult, String languageName) {
    MovieDescriptor movie = (MovieDescriptor) searchResult;

    List<SubtitleDescriptor> subtitles = new ArrayList<>();

    // retrieve subtitles by name and year
    for(Subtitle subtitle : getSubtitleList(null, movie.getName(), movie.getYear(), null, null, languageName)) {
      subtitles.add(new SublightSubtitleDescriptor(subtitle, this));
    }

    return subtitles;
  }

  public List<SubtitleDescriptor> getSubtitleList(String name, Integer year, Short season, Integer episode, String languageName) {
    List<SubtitleDescriptor> subtitles = new ArrayList<>();

    // retrieve subtitles by name and year
    for(Subtitle subtitle : getSubtitleList(null, name, year, season, episode, languageName)) {
      subtitles.add(new SublightSubtitleDescriptor(subtitle, this));
    }

    return subtitles;
  }

  private List<Subtitle> getSubtitleList(String videoHash, String name, Integer year, Short season, Integer episode, String languageName) {
    // require login
    login();

    // given language or all languages
    ArrayOfSubtitleLanguage languages = new ArrayOfSubtitleLanguage();

    if(languageName != null) {
      // given language
      languages.getSubtitleLanguage().add(getSubtitleLanguage(languageName));
    }
    else {
      // all languages
      Collections.addAll(languages.getSubtitleLanguage(), SubtitleLanguage.values());
    }

    // hash singleton array
    ArrayOfString videoHashes = new ArrayOfString();
    videoHashes.getString().add(videoHash);

    // all genres
    ArrayOfGenre genres = new ArrayOfGenre();
    Collections.addAll(genres.getGenre(), Genre.values());

    // response holders
    Holder<ArrayOfSubtitle> subtitles = new Holder<>();
    Holder<ArrayOfRelease> releases = new Holder<>();
    Holder<String> error = new Holder<>();

    System.out.println("[FINE] SublightSubtitleClient.getSubtitleList() - name=" + name + "; year=" + year);

    webservice.searchSubtitles4(session, videoHashes, name, year, season, episode, languages, genres, null, null, null, subtitles, releases, null, error);

    // abort if something went wrong
    checkError(error);

    // return empty list if response is empty
    if(subtitles.value == null) {
      return Collections.emptyList();
    }

    // map all release names by subtitle id
    if(releases.value != null) {
      Map<String, String> releaseNameBySubtitleID = new HashMap<>();

      // map release names by subtitle id
      for(Release release : releases.value.getRelease()) {
        releaseNameBySubtitleID.put(release.getSubtitleID(), release.getName());
      }

      // set release names
      for(Subtitle subtitle : subtitles.value.getSubtitle()) {
        subtitle.setRelease(releaseNameBySubtitleID.get(subtitle.getSubtitleID()));
      }
    }

    return subtitles.value.getSubtitle();
  }

  private static Map<String, SubtitleLanguage> getLanguageAliasMap() {
    Map<String, SubtitleLanguage> languages = new HashMap<>(4);

    // insert special some additional special handling
    languages.put("Brazilian", SubtitleLanguage.PORTUGUESE_BRAZIL);
    languages.put("Bosnian", SubtitleLanguage.BOSNIAN_LATIN);
    languages.put("Serbian", SubtitleLanguage.SERBIAN_LATIN);

    return languages;
  }

  private static SubtitleLanguage getSubtitleLanguage(String languageName) {
    // check subtitle language enum
    for(SubtitleLanguage language : SubtitleLanguage.values()) {
      if(language.value().equalsIgnoreCase(languageName)) {
        return language;
      }
    }

    // check alias list
    for(Entry<String, SubtitleLanguage> alias : getLanguageAliasMap().entrySet()) {
      if(alias.getKey().equalsIgnoreCase(languageName)) {
        return alias.getValue();
      }
    }

    // illegal language name
    throw new IllegalArgumentException("Illegal language: " + languageName);
  }

  public static String getLanguageName(SubtitleLanguage language) {
    // check alias list first
    for(Entry<String, SubtitleLanguage> alias : getLanguageAliasMap().entrySet()) {
      if(language == alias.getValue()) {
        return alias.getKey();
      }
    }

    // use language value by default
    return language.value();
  }

  public byte[] getZipArchive(Subtitle subtitle) throws InterruptedException {
    // require login
    login();

    Holder<String> ticket = new Holder<>();
    Holder<Short> que = new Holder<>();
    Holder<byte[]> data = new Holder<>();
    Holder<String> error = new Holder<>();

    webservice.getDownloadTicket2(session, null, subtitle.getSubtitleID(), null, ticket, que, null, error);

    // abort if something went wrong
    checkError(error);

    // wait x seconds as specified by the download ticket response, download ticket is not valid until then
    Thread.sleep(que.value * 1000L);

    webservice.downloadByID4(session, subtitle.getSubtitleID(), -1, false, ticket.value, null, data, null, error);

    // abort if something went wrong
    checkError(error);

    // return zip file bytes
    return data.value;
  }

  private synchronized void login() {
    if(webservice == null) {
      // lazy initialize because all the JAX-WS class loading can take quite some time
      webservice = new Sublight().getSublightSoap();
    }

    if(session == null) {
      // args contains only iid
      ArrayOfString args = new ArrayOfString();
      args.getString().add(IID);

      Holder<String> session = new Holder<>();
      Holder<String> error = new Holder<>();

      webservice.logInAnonymous4(clientInfo, args, session, null, error);

      // abort if something went wrong
      checkError(error);

      // start session
      this.session = session.value;
    }

    // reset timer
    logoutTimer.set(10, TimeUnit.MINUTES, true);
  }

  private synchronized void logout() {
    if(session != null) {
      Holder<String> error = new Holder<>();

      webservice.logOut(session, null, error);

      // abort if something went wrong
      checkError(error);

      // stop session
      this.session = null;

      // cancel timer
      logoutTimer.cancel();
    }
  }

  private static void checkError(Holder<?> error) {
    if(error.value != null) {
      throw new WebServiceException("Response indicates error: " + error.value);
    }
  }

  private final Timer logoutTimer = new Timer() {
    @Override
    public void run() {
      logout();
    }
  };
}
