package net.sf.jtmdb;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.jtmdb.GeneralSettings.Utilities;
import net.sf.jtmdb.Log.Verbosity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is the class that represents a Person. It also provides static methods
 * for searching for a person and for getting a specific person. Has two
 * "flavors". The normal one and a reduced one. The reduced is returned when
 * searching for people and is missing some fields that are present when getting
 * the info for a specific person.
 *
 * @author Savvas Dalkitsis
 */
public class Person implements Serializable {

  private static final long serialVersionUID = 316786806074114033L;

  /**
   * The name of the person.
   */
  private String name;
  /**
   * The profile image of the person.
   */
  private PersonProfile profile;
  /**
   * The url of the person.
   */
  private URL url;
  /**
   * The ID of the person.
   */
  private int ID;
  /**
   * The biography of the person.
   */
  private String biography;
  /**
   * The popularity of the Person;
   */
  private int popularity;
  /**
   * The json string that created this Person object.
   */
  private String jsonOrigin;

  /**
   * Denotes whether the person object is reduced.
   */
  private boolean isReduced;
  /**
   * The date of the last modification.
   */
  private Date lastModifiedAt;
  /**
   * The version of the Person.
   */
  private int version;

  // Only in full profile

  /**
   * The birthplace of the person. Not present in reduced form.
   */
  private String birthPlace;
  /**
   * The number of known movies of the person. Not present in reduced form.
   */
  private int knownMovies;
  /**
   * The filmography of the person. Not present in reduced form.
   */
  private Set<FilmographyInfo> filmography = new LinkedHashSet<>();
  /**
   * The birthday of the person. Not present in reduced form.
   */
  private Date birthday;
  /**
   * The aliases of the person. Not present in reduced form.
   */
  private Set<String> aka = new LinkedHashSet<>();

  /**
   * Construct a person object from a JSON object.
   *
   * @param jsonObject
   *            The JSON object describing the Person.
   */
  public Person(JSONObject jsonObject) {
    Log.log("Creating Person object from JSONObject", Verbosity.VERBOSE);
    parseJSON(jsonObject);
  }

  /**
   *
   * Construct a person object from a JSON array containing the JSON object
   * describing the Person.
   *
   * @param jsonObjectInArray
   *            A JSON array containing the JSON object describing the Person.
   */
  public Person(JSONArray jsonObjectInArray) {
    Log.log("Creating Person object from JSONArray", Verbosity.VERBOSE);
    parseJSON(jsonObjectInArray);
  }

  /**
   * The json string that created this Person object.
   *
   * @return The json string that created this Person object.
   */
  public String getJsonOrigin() {
    return jsonOrigin;
  }

  /**
   * The prettyprinted json string that created this Person object.
   *
   * @param indentFactor
   *            The number of spaces to add to each level of indentation.
   * @return The json string that created this Person object.
   */
  public String getJsonOrigin(int indentFactor) {
    try {
      return new JSONObject(jsonOrigin).toString(indentFactor);
    } catch (JSONException e) {
      Log.log(e, Verbosity.ERROR);
      return null;
    }
  }

  /**
   * The name of the person.
   *
   * @return The name of the person.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the person.
   *
   * @param name
   *            The name of the person.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The birthplace of the person. Not present in reduced form (see class
   * description {@link Person} and method {@link #isReduced()}).
   *
   * @return The birthplace of the person.
   */
  public String getBirthPlace() {
    return birthPlace;
  }

  /**
   * Sets the birthplace of the person.
   *
   * @param birthPlace
   *            The birthplace of the person.
   */
  public void setBirthPlace(String birthPlace) {
    this.birthPlace = birthPlace;
  }

  /**
   * The profile image of the person.
   *
   * @return The profile image of the person.
   */
  public PersonProfile getProfile() {
    return profile;
  }

  /**
   * Sets the profile image of the person.
   *
   * @param profile
   *            The profile image of the person.
   */
  public void setProfile(PersonProfile profile) {
    this.profile = profile;
  }

  /**
   * The number of known movies of the person. Not present in reduced form
   * (see class description {@link Person} and method {@link #isReduced()}).
   *
   * @return The number of known movies of the person.
   */
  public int getKnownMovies() {
    return knownMovies;
  }

  /**
   * Sets the number of known movies of the person.
   *
   * @param knownMovies
   *            The number of known movies of the person.
   */
  public void setKnownMovies(int knownMovies) {
    this.knownMovies = knownMovies;
  }

  /**
   * The filmography of the person. Not present in reduced form (see class
   * description {@link Person} and method {@link #isReduced()}).
   *
   * @return The filmography of the person.
   */
  public Set<FilmographyInfo> getFilmography() {
    return filmography;
  }

  /**
   * Sets the filmography of the person.
   *
   * @param filmography
   *            The filmography of the person.
   */
  public void setFilmography(Set<FilmographyInfo> filmography) {
    this.filmography = filmography;
  }

  /**
   * The url of the person.
   *
   * @return The url of the person.
   */
  public URL getUrl() {
    return url;
  }

  /**
   * Sets the url of the person.
   *
   * @param url
   *            The url of the person.
   */
  public void setUrl(URL url) {
    this.url = url;
  }

  /**
   * The ID of the person.
   *
   * @return The ID of the person.
   */
  public int getID() {
    return ID;
  }

  /**
   * Sets the popularity of the person.
   *
   * @param popularity
   *            The popularity of the person.
   */
  public void setPopularity(int popularity) {
    this.popularity = popularity;
  }

  /**
   * The popularity of the person.
   *
   * @return The popularity of the person.
   */
  public int getPopularity() {
    return popularity;
  }

  /**
   * Sets the ID of the person.
   *
   * @param iD
   *            The ID of the person.
   */
  public void setID(int iD) {
    ID = iD;
  }

  /**
   * The birthday of the person. Not present in reduced form (see class
   * description {@link Person} and method {@link #isReduced()}).
   *
   * @return The birthday of the person.
   */
  public Date getBirthday() {
    return birthday;
  }

  /**
   * Sets the birthday of the person.
   *
   * @param birthday
   *            The birthday of the person.
   */
  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  /**
   * The aliases of the person. Not present in reduced form (see class
   * description {@link Person} and method {@link #isReduced()}).
   *
   * @return The aliases of the person.
   */
  public Set<String> getAka() {
    return aka;
  }

  /**
   * Sets the aliases of the person.
   *
   * @param aka
   *            The aliases of the person.
   */
  public void setAka(Set<String> aka) {
    this.aka = aka;
  }

  /**
   * Sets whether the Person contains reduced information (see class
   * description {@link Person}).
   *
   * @param isReduced
   *            True if the Person has reduced fields set.
   */
  public void setReduced(boolean isReduced) {
    this.isReduced = isReduced;
  }

  /**
   * If true, the Person object has reduced fields set (see class description
   * {@link Person}).
   *
   * @return True if the Person has reduced fields set.
   */
  public boolean isReduced() {
    return isReduced;
  }

  /**
   * The biography of the person.
   *
   * @return The biography of the person.
   */
  public String getBiography() {
    return biography;
  }

  /**
   * Sets the biography of the person.
   *
   * @param biography
   *            The biography of the person.
   */
  public void setBiography(String biography) {
    this.biography = biography;
  }

  /**
   * The Date of the last modification.
   *
   * @return The Date of the last modification.
   */
  public Date getLastModifiedAtDate() {
    return lastModifiedAt;
  }

  /**
   * Sets the date of the last modification.
   *
   * @param lastModifiedAt
   *            The date of the last modification.
   */
  public void setLastModifiedAtDate(Date lastModifiedAt) {
    this.lastModifiedAt = lastModifiedAt;
  }

  /**
   * The version of the Person.
   *
   * @return The version of the Person.
   */
  public int getVersion() {
    return version;
  }

  /**
   * Sets the version of the Person.
   *
   * @param version
   *            The version of the Person.
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * Parses a JSON object wrapped in a JSON array and sets the Person fields.
   *
   * @param jsonArray
   *            The JSON array containing the JSON object that describes the
   *            Person.
   */
  public boolean parseJSON(JSONArray jsonArray) {
    try {
      return parseJSON(jsonArray.getJSONObject(0));
    } catch (JSONException e) {
      Log.log(e, Verbosity.ERROR);
    }
    return false;
  }

  /**
   * Parses a JSON object and sets the Person fields.
   *
   * @param jsonObject
   *            The JSON object that describes the Person.
   */
  public boolean parseJSON(JSONObject jsonObject) {
    try {
      jsonOrigin = jsonObject.toString();
      setPopularity(jsonObject.getInt("popularity"));
      setName(jsonObject.getString("name"));
      try {
        setUrl(new URL(jsonObject.getString("url")));
      } catch (MalformedURLException e) {
        Log.log(e, Verbosity.ERROR);
        setUrl(null);
      }
      setID(jsonObject.getInt("id"));
      JSONArray profile = jsonObject.getJSONArray("profile");
      for (int i = 0; i < profile.length(); i++) {
        JSONObject p = profile.getJSONObject(i).getJSONObject("image");
        int profileW = -1;
        int profileH = -1;
        try {
          profileW = p.getInt("width");
        } catch (JSONException e) {
          Log.log(e, Verbosity.ERROR);
        }
        try {
          profileH = p.getInt("height");
        } catch (JSONException e) {
          Log.log(e, Verbosity.ERROR);
        }
        Dimension profileD = null;
        if (profileW > 0 && profileH > 0) {
          profileD = new Dimension(profileW, profileH);
        }
        String id = p.getString("id");
        URL url = null;
        try {
          url = new URL(p.getString("url"));
        } catch (MalformedURLException e) {
          Log.log(e, Verbosity.ERROR);
          e.printStackTrace();
        }
        String size = p.getString("size");
        PersonProfile.Size ps = PersonProfile.Size.ORIGINAL;
        if (size.equalsIgnoreCase("thumb")) {
          ps = PersonProfile.Size.THUMB;
        } else if (size.equalsIgnoreCase("profile")) {
          ps = PersonProfile.Size.PROFILE;
        }
        PersonProfile prof = getProfile();
        if (prof == null) {
          prof = new PersonProfile(id);
        }
        prof.setImage(ps, new Pair<>(profileD, url));
        setProfile(prof);
      }
      Date lastModified = null;
      try {
        lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        .parse(jsonObject.getString("last_modified_at"));
      } catch (ParseException e) {
        Log.log(e, Verbosity.ERROR);
      }
      if (lastModified != null) {
        setLastModifiedAtDate(lastModified);
      }
      setVersion(jsonObject.getInt("version"));

      setReduced(true);
      if (jsonObject.has("biography")) {
        setReduced(false);
        setBiography(jsonObject.getString("biography"));
        setBirthPlace(jsonObject.getString("birthplace"));
        setKnownMovies(jsonObject.getInt("known_movies"));
        Date date = null;
        try {
          date = new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject
            .getString("birthday"));
        } catch (ParseException e) {
          Log.log(e, Verbosity.ERROR);
        }
        if (date != null) {
          setBirthday(date);
        }
        JSONArray knownAs = jsonObject.getJSONArray("known_as");
        for (int i = 0; i < knownAs.length(); i++) {
          getAka().add(knownAs.getJSONObject(i).getString("name"));
        }
        JSONArray filmArray = jsonObject.getJSONArray("filmography");
        for (int i = 0; i < filmArray.length(); i++) {
          JSONObject film = filmArray.getJSONObject(i);
          String filmName = film.getString("name");
          String filmCharacter = film.getString("character");
          URL filmUrl = null;
          try {
            filmUrl = new URL(film.getString("url"));
          } catch (MalformedURLException e) {
            Log.log(e, Verbosity.ERROR);
          }
          int filmID = film.getInt("id");
          int castID = film.getInt("cast_id");
          String filmJob = film.getString("job");
          String filmDepartment = film.getString("department");
          URL moviePoster = null;
          try {
            moviePoster = new URL(film.getString("poster"));
          } catch (MalformedURLException e) {
            Log.log(e, Verbosity.ERROR);
          }
          boolean filmAdult = film.getBoolean("adult");

          Date releasedDate = null;
          try {
            releasedDate = new SimpleDateFormat("yyyy-MM-dd")
            .parse(film.getString("release"));
          } catch (ParseException e) {
            Log.log(e, Verbosity.ERROR);
          }
          getFilmography().add(
            new FilmographyInfo(filmName, filmCharacter,
              filmUrl, filmID, castID, filmJob,
              filmDepartment, film.toString(),
              moviePoster, filmAdult, releasedDate));
        }
      }
    } catch (JSONException e) {
      Log.log(e, Verbosity.ERROR);
    }
    return false;
  }

  /**
   * Searches for people and returns full flavors. The string supplied can
   * contain spaces. Returns a list of Person objects with the full form (see
   * class description {@link Person} and method {@link #isReduced()}). Will
   * return null if a valid API key was not supplied to the
   * {@link GeneralSettings}
   *
   * @param name
   *            The name of the person to search for.
   * @return A list of Person objects with the full form (see class
   *         description {@link Person} and method {@link #isReduced()}).Will
   *         return null if a valid API key was not supplied to the
   *         {@link GeneralSettings}
   * @throws IOException
   * @throws JSONException
   */
  public static List<Person> deepSearch(String nameParameter) throws IOException, JSONException {
    Log.log("Performing a deep Person search for \"" + nameParameter + "\"", Verbosity.NORMAL);
    String name;

    try {
      name = URLEncoder.encode(nameParameter, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    if (GeneralSettings.getApiKey() != null
        && !GeneralSettings.getApiKey().equals("")) {
      if (name != null && !name.equals("")) {
        try {
          URL call = new URL(GeneralSettings.BASE_URL
            + GeneralSettings.PERSON_SEARCH_URL
            + GeneralSettings.getAPILocaleFormatted() + "/"
            + GeneralSettings.API_MODE_URL + "/"
            + GeneralSettings.getApiKey() + "/" + name);
          String jsonString = Utilities.readUrlResponse(call).trim();
          List<Person> results = new LinkedList<>();
          if ((jsonString.startsWith("[") || jsonString
              .startsWith("{"))
              && !jsonString.equals("[\"Nothing found.\"]")) {
            JSONArray jsonArray = new JSONArray(jsonString
              .toString());
            for (int i = 0; i < jsonArray.length(); i++) {
              results.add(getInfo(jsonArray.getJSONObject(i)
                .getInt("id")));
            }
          } else {
            Log.log("Search for \"" + name
              + "\" returned no results", Verbosity.NORMAL);
          }
          return results;
        } catch (IOException e) {
          Log.log(e, Verbosity.ERROR);
          throw e;
        } catch (JSONException e) {
          Log.log(e, Verbosity.ERROR);
          throw e;
        }
      }

      Log.log("Cannot search for a null or empty string", Verbosity.ERROR);
    }

    Log.log("Error with the API key", Verbosity.ERROR);

    return null;
  }

  /**
   * Searches for people. The string supplied can contain spaces. Returns a
   * list of Person objects with the reduced form (see class description
   * {@link Person} and method {@link #isReduced()}). Will return null if a
   * valid API key was not supplied to the {@link GeneralSettings}
   *
   * @param name
   *            The name of the person to search for.
   * @return A list of Person objects with the reduced form (see class
   *         description {@link Person} and method {@link #isReduced()}).Will
   *         return null if a valid API key was not supplied to the
   *         {@link GeneralSettings}
   * @throws IOException
   * @throws JSONException
   */
  public static List<Person> search(String nameParameter) throws IOException, JSONException {
    Log.log("Performing a Person search for \"" + nameParameter + "\"", Verbosity.NORMAL);
    String name;

    try {
      name = URLEncoder.encode(nameParameter, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    if (GeneralSettings.getApiKey() != null
        && !GeneralSettings.getApiKey().equals("")) {
      if (name != null && !name.equals("")) {
        try {
          URL call = new URL(GeneralSettings.BASE_URL
            + GeneralSettings.PERSON_SEARCH_URL
            + GeneralSettings.getAPILocaleFormatted() + "/"
            + GeneralSettings.API_MODE_URL + "/"
            + GeneralSettings.getApiKey() + "/" + name);
          String jsonString = Utilities.readUrlResponse(call).trim();
          List<Person> results = new LinkedList<>();
          if ((jsonString.startsWith("[") || jsonString
              .startsWith("{"))
              && !jsonString.equals("[\"Nothing found.\"]")) {
            JSONArray jsonArray = new JSONArray(jsonString
              .toString());
            for (int i = 0; i < jsonArray.length(); i++) {
              results.add(new Person(jsonArray.getJSONObject(i)));
            }
          } else {
            Log.log("Search for \"" + name
              + "\" returned no results", Verbosity.NORMAL);
          }
          return results;
        }
        catch (IOException e) {
          Log.log(e, Verbosity.ERROR);
          throw e;
        }
        catch (JSONException e) {
          Log.log(e, Verbosity.ERROR);
          throw e;
        }
      }

      Log.log("Cannot search for a null or empty string", Verbosity.ERROR);
    }
    else {
      Log.log("Error with the API key", Verbosity.ERROR);
    }
    return null;
  }

  /**
   * Gets the info for a specific Person (by ID). Returns a Person object with
   * the normal form (see class description {@link Person} and method
   * {@link #isReduced()}). Will return null if a valid API key was not
   * supplied to the {@link GeneralSettings} or if the supplied ID did not
   * correspond to a Person.
   *
   * @param ID
   *            The ID of the Person.
   * @return A Person object with the normal form (see class description
   *         {@link Person} and method {@link #isReduced()}). Will return null
   *         if a valid API key was not supplied to the
   *         {@link GeneralSettings} or if the supplied ID did not correspond
   *         to a Person.
   * @throws IOException
   * @throws JSONException
   */
  public static Person getInfo(int ID) throws IOException, JSONException {
    Log.log("Getting info for Person with id " + ID, Verbosity.NORMAL);
    if (GeneralSettings.getApiKey() != null && !GeneralSettings.getApiKey().equals("")) {
      try {
        URL call = new URL(GeneralSettings.BASE_URL
          + GeneralSettings.PERSON_GETINFO_URL
          + GeneralSettings.getAPILocaleFormatted() + "/"
          + GeneralSettings.API_MODE_URL + "/"
          + GeneralSettings.getApiKey() + "/" + ID);
        String jsonString = Utilities.readUrlResponse(call).trim();
        if ((jsonString.startsWith("[") || jsonString.startsWith("{")) && !jsonString.equals("[\"Nothing found.\"]")) {
          JSONArray jsonArray = new JSONArray(jsonString.toString());
          return new Person(jsonArray);
        }

        Log.log("Getting info for Person with ID " + ID + " returned no results", Verbosity.NORMAL);
      }
      catch (IOException e) {
        Log.log(e, Verbosity.ERROR);
        throw e;
      }
      catch (JSONException e) {
        Log.log(e, Verbosity.ERROR);
        throw e;
      }
    }
    else {
      Log.log("Error with the API key", Verbosity.ERROR);
    }
    return null;
  }

  /**
   * Gets the version information for a Person by ID. Will return null if a
   * valid API key was not supplied to the {@link GeneralSettings} or if the
   * supplied ID did not correspond to a Person.
   *
   * @param ID
   *            The ID of the Person to get the version for.
   * @return Version information of a Person by ID.
   * @throws IOException
   * @throws JSONException
   */
  public static PersonVersionInfo getVersion(int ID) throws IOException,
  JSONException {
    Log.log("Getting version for Person with id " + ID, Verbosity.NORMAL);
    if (GeneralSettings.getApiKey() != null
        && !GeneralSettings.getApiKey().equals("")) {
      try {
        URL call = new URL(GeneralSettings.BASE_URL
          + GeneralSettings.PERSON_GETVERSION_URL
          + GeneralSettings.getAPILocaleFormatted() + "/"
          + GeneralSettings.API_MODE_URL + "/"
          + GeneralSettings.getApiKey() + "/" + ID);
        String jsonString = Utilities.readUrlResponse(call).trim();
        if ((jsonString.startsWith("[") || jsonString.startsWith("{"))
            && !jsonString.equals("[\"Nothing found.\"]")) {
          JSONArray jsonArray = new JSONArray(jsonString.toString());
          JSONObject jsonObject = jsonArray.getJSONObject(0);
          String name = jsonObject.getString("name");
          int movieID = jsonObject.getInt("id");
          int version = jsonObject.getInt("version");
          Date lastModified = null;
          try {
            lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonObject.getString("last_modified_at"));
          }
          catch (ParseException e) {
            Log.log(e, Verbosity.ERROR);
          }
          return new PersonVersionInfo(name, movieID, version,
            lastModified);
        }

        Log.log("Getting version for Person with id " + ID + " returned no results", Verbosity.NORMAL);

      }
      catch (IOException e) {
        Log.log(e, Verbosity.ERROR);
        throw e;
      }
      catch (JSONException e) {
        Log.log(e, Verbosity.ERROR);
        throw e;
      }
    }
    else {
      Log.log("Error with the API key", Verbosity.ERROR);
    }
    return null;
  }

  /**
   * Gets the version information for a list of Persons by ID. Will return
   * null if a valid API key was not supplied to the {@link GeneralSettings}
   * and will skip any IDs that did not correspond to a Person.
   *
   * @param IDs
   *            The list of Person IDs to get version information for.
   * @return Version information of a list of Persons by ID.
   * @throws IOException
   * @throws JSONException
   */
  public static List<PersonVersionInfo> getVersion(int... IDs)
      throws IOException, JSONException {
    if (IDs == null || IDs.length == 0) {
      Log.log("Provided empty or null list of IDs for Person.getVersion",
        Verbosity.ERROR);
      return null;
    }
    StringBuffer listIDs = new StringBuffer();
    for (int ID : IDs) {
      listIDs.append("," + ID);
    }
    listIDs.delete(0, 1);
    Log.log("Getting version for Persons with ids " + listIDs.toString(),
      Verbosity.NORMAL);
    if (GeneralSettings.getApiKey() != null
        && !GeneralSettings.getApiKey().equals("")) {
      try {
        URL call = new URL(GeneralSettings.BASE_URL
          + GeneralSettings.PERSON_GETVERSION_URL
          + GeneralSettings.getAPILocaleFormatted() + "/"
          + GeneralSettings.API_MODE_URL + "/"
          + GeneralSettings.getApiKey() + "/" + listIDs);
        String jsonString = Utilities.readUrlResponse(call).trim();
        if ((jsonString.startsWith("[") || jsonString.startsWith("{"))
            && !jsonString.equals("[\"Nothing found.\"]")) {
          JSONArray jsonArray = new JSONArray(jsonString.toString());
          List<PersonVersionInfo> versionInfo = new LinkedList<>();
          for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("name");
            int movieID = jsonObject.getInt("id");
            int version = jsonObject.getInt("version");
            Date lastModified = null;
            try {
              lastModified = new SimpleDateFormat(
                  "yyyy-MM-dd HH:mm:ss").parse(jsonObject
                    .getString("last_modified_at"));
            }
            catch (ParseException e) {
              Log.log(e, Verbosity.ERROR);
            }
            versionInfo.add(new PersonVersionInfo(name, movieID, version, lastModified));
          }
          return versionInfo;
        }

        Log.log("Getting version for list of Persons with ids " + listIDs + " returned no results", Verbosity.NORMAL);
      }
      catch (IOException e) {
        Log.log(e, Verbosity.ERROR);
        throw e;
      }
      catch (JSONException e) {
        Log.log(e, Verbosity.ERROR);
        throw e;
      }
    }
    else {
      Log.log("Error with the API key", Verbosity.ERROR);
    }
    return null;
  }

  /**
   * Gets the version information for the last Person in the database. Will
   * return null if a valid API key was not supplied to the
   * {@link GeneralSettings} or if the supplied ID did not correspond to a
   * Person.
   *
   * @return Version information for the last Person in the database.
   * @throws IOException
   * @throws JSONException
   */
  public static PersonVersionInfo getLatest() throws IOException,
  JSONException {
    Log.log("Getting latest Person", Verbosity.NORMAL);
    if (GeneralSettings.getApiKey() != null && !GeneralSettings.getApiKey().equals("")) {
      try {
        URL call = new URL(GeneralSettings.BASE_URL
          + GeneralSettings.PERSON_GETLATEST_URL
          + GeneralSettings.getAPILocaleFormatted() + "/"
          + GeneralSettings.API_MODE_URL + "/"
          + GeneralSettings.getApiKey());
        String jsonString = Utilities.readUrlResponse(call).trim();
        if ((jsonString.startsWith("[") || jsonString.startsWith("{")) && !jsonString.equals("[\"Nothing found.\"]")) {
          JSONArray jsonArray = new JSONArray(jsonString.toString());
          JSONObject jsonObject = jsonArray.getJSONObject(0);
          String name = jsonObject.getString("name");
          int movieID = jsonObject.getInt("id");
          int version = jsonObject.getInt("version");
          Date lastModified = null;
          try {
            lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(jsonObject.getString("last_modified_at"));
          }
          catch (ParseException e) {
            Log.log(e, Verbosity.ERROR);
          }
          return new PersonVersionInfo(name, movieID, version, lastModified);
        }

        Log.log("Getting latest Person entry returned no results", Verbosity.NORMAL);
      }
      catch (IOException e) {
        Log.log(e, Verbosity.ERROR);
        throw e;
      }
      catch (JSONException e) {
        Log.log(e, Verbosity.ERROR);
        throw e;
      }
    }
    else {
      Log.log("Error with the API key", Verbosity.ERROR);
    }
    return null;
  }
}
