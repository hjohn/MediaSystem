package net.sf.jtmdb;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.jtmdb.GeneralSettings.Utilities;
import net.sf.jtmdb.Log.Verbosity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents a movie Genre.
 *
 * @author Savvas Dalkitsis
 */
public class Genre implements Serializable {

  private static final long serialVersionUID = 1450989769066278063L;

  /**
   * The url of the Genre.
   */
  private URL url;
  /**
   * The name of the Genre.
   */
  private String name;
  /**
   * The ID of the Genre.
   */
  private int ID;

  /**
   * Constructs a Genre with the given URL, name and ID.
   *
   * @param url
   *            The URL of the Genre.
   * @param name
   *            The name of the Genre.
   * @param ID
   *            The ID of the Genre.
   */
  public Genre(URL url, String name, int ID) {
    Log.log("Creating Genre object with url: "
        + ((url == null) ? "NULL" : url.toString()) + ", name: " + name
        + " and ID: " + ID, Verbosity.VERBOSE);
    setUrl(url);
    setName(name);
    setID(ID);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof Genre) {
      return ((Genre) obj).getID() == getID();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getID();
  }

  /**
   * The URL of the Genre.
   *
   * @return The URL of the Genre.
   */
  public URL getUrl() {
    return url;
  }

  /**
   * Sets the URL of the Genre.
   *
   * @param url
   *            The URL of the Genre.
   */
  public void setUrl(URL url) {
    this.url = url;
  }

  /**
   * The name of the Genre.
   *
   * @return The name of the Genre.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the Genre.
   *
   * @param name
   *            The name of the Genre.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The ID of the Genre.
   *
   * @return The ID of the Genre.
   */
  public int getID() {
    return ID;
  }

  /**
   * Sets the ID of the Genre.
   *
   * @param ID
   *            The ID of the Genre.
   */
  public void setID(int ID) {
    this.ID = ID;
  }

  /**
   * Gets the list of valid genres within TMDb. You can also request the
   * translated values by setting the language option in the GeneralSettings.
   * Returns a Pair of objects. The first object is a boolean that denotes if
   * the list is translated. The second is the list of Genres. Will return
   * null if a valid API key was not supplied to the {@link GeneralSettings}.
   *
   * @return Returns a Pair of objects. The first object is a boolean that
   *         denotes if the list is translated. The second is the list of
   *         Genres. Will return null if a valid API key was not supplied to
   *         the {@link GeneralSettings}.
   * @throws IOException
   * @throws JSONException
   */
  public static Pair<Boolean, Set<Genre>> getList() throws IOException,
  JSONException {
    Log.log("Getting list of Genres", Verbosity.NORMAL);
    if (GeneralSettings.getApiKey() != null
        && !GeneralSettings.getApiKey().equals("")) {
      try {
        URL call = new URL(GeneralSettings.BASE_URL
            + GeneralSettings.GENRES_GETLIST_URL
            + GeneralSettings.getAPILocaleFormatted() + "/"
            + GeneralSettings.API_MODE_URL + "/"
            + GeneralSettings.getApiKey());
        String jsonString = Utilities.readUrlResponse(call).trim();
        if ((jsonString.startsWith("[") || jsonString.startsWith("{"))
            && !jsonString.equals("[\"Nothing found.\"]")) {
          JSONArray jsonArray = new JSONArray(jsonString.toString());
          JSONObject jsonObject = jsonArray.getJSONObject(0);
          boolean translated = jsonObject.getBoolean("translated");
          Set<Genre> genres = new LinkedHashSet<>();
          for (int i = 1; i < jsonArray.length(); i++) {
            jsonObject = jsonArray.getJSONObject(i);
            String genreName = jsonObject.getString("name");
            URL genreUrl = null;
            try {
              genreUrl = new URL(jsonObject.getString("url"));
            }
            catch (MalformedURLException e) {
              Log.log(e, Verbosity.ERROR);
            }
            int ID = jsonObject.getInt("id");
            genres.add(new Genre(genreUrl, genreName, ID));
          }
          return new Pair<>(translated, genres);
        }

        Log.log("Getting list of Genres returned no results", Verbosity.NORMAL);
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

  @Override
  public String toString() {
    return name;
  }
}
