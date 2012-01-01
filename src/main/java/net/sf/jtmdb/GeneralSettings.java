package net.sf.jtmdb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * This class contains general settings for the library (the API key for
 * instance)
 *
 * @author Savvas Dalkitsis
 */
public class GeneralSettings {

	/**
	 * The base url of all API calls.
	 */
	protected static final String BASE_URL = "http://api.themoviedb.org/2.1/";
	/**
	 * The url of the API method for getting Movie images.
	 */
	protected static final String MOVIE_GETIMAGES_URL = "Movie.getImages/";
	/**
	 * The url of the API method for searching for Movie.
	 */
	protected static final String MOVIE_SEARCH_URL = "Movie.search/";
	/**
	 * The url of the API method for getting Movie info.
	 */
	protected static final String MOVIE_GETINFO_URL = "Movie.getInfo/";

	 protected static final String MOVIE_IMDB_LOOKUP_URL = "Movie.imdbLookup/";

	/**
	 * The url of the API method for getting Movie version.
	 */
	protected static final String MOVIE_GETVERSION_URL = "Movie.getVersion/";
	/**
	 * The url of the API method for getting the latest Movie entry.
	 */
	protected static final String MOVIE_GETLATEST_URL = "Movie.getLatest/";
	/**
	 * The url of the API method for getting the translations of a Movie.
	 */
	protected static final String MOVIE_GETTRANSLATIONS_URL = "Movie.getTranslations/";
	/**
	 * The url of the API method for browsing for Movies.
	 */
	protected static final String MOVIE_BROWSE_URL = "Movie.browse/";
	/**
	 * The url of the API method for rating Movies.
	 */
	protected static final String MOVIE_ADD_RATING_URL = "Movie.addRating";
	/**
	 * The url of the API method for searching for Person.
	 */
	protected static final String PERSON_SEARCH_URL = "Person.search/";
	/**
	 * The url of the API method for getting Person info.
	 */
	protected static final String PERSON_GETINFO_URL = "Person.getInfo/";
	/**
	 * The url of the API method for getting Person version.
	 */
	protected static final String PERSON_GETVERSION_URL = "Person.getVersion/";
	/**
	 * The url of the API method for getting the latest Person entry.
	 */
	protected static final String PERSON_GETLATEST_URL = "Person.getLatest/";
	/**
	 * The url of the API method for getting the list of Genres.
	 */
	protected static final String GENRES_GETLIST_URL = "Genres.getList/";
	/**
	 * The url of the API method for getting the token for authentication.
	 */
	protected static final String AUTH_TOKEN_URL = "Auth.getToken/";
	/**
	 * The url of the API method for getting the session from a token.
	 */
	protected static final String AUTH_SESSION_URL = "Auth.getSession/";
	/**
	 * The url of the API method for getting Movie info from media.
	 */
	protected static final String MEDIA_GETINFO_URL = "Media.getInfo/";
	/**
	 * The url of the API method for adding Media ID for a Movie.
	 */
	protected static final String MEDIA_ADD_ID_URL = "Media.addID";
	/**
	 * The url of the API mode used.
	 */
	protected static final String API_MODE_URL = "json";
	/**
	 * The url of the home page of the movie database.
	 */
	protected static final String HOMEPAGE_URL = "http://www.themoviedb.org/";

	private GeneralSettings() {
	}

	/**
	 * The API key.
	 */
	private static String apiKey = "";
	/**
	 * The stream in which to output the log.
	 */
	private static PrintStream logStream = System.out;
	/**
	 * Denotes whether to output to the log.
	 */
	private static boolean logEnabled = false;
	/**
	 * The verbosity level of the log.
	 */
	private static Log.Verbosity logVerbosity = Log.Verbosity.VERBOSE;
	/**
	 * The locale of the API.
	 */
	private static Locale APILocale = new Locale("en", "US");

	/**
	 * Returns the stored API key for the library. Used by search methods
	 * internally.
	 *
	 * @return The stored API key for the library.
	 */
	public static String getApiKey() {
		return apiKey;
	}

	/**
	 * Sets the API key for the library. Used by search methods internally.
	 *
	 * @param apiKey
	 *            The API key for the library.
	 */
	public static void setApiKey(String apiKey) {
		GeneralSettings.apiKey = apiKey;
	}

	/**
	 * Sets the output stream to use for the log.
	 *
	 * @param logStream
	 *            The output stream to use for the log.
	 */
	public static void setLogStream(PrintStream logStream) {
		GeneralSettings.logStream = logStream;
	}

	/**
	 * Convenience method for setting the output stream to use for the log into
	 * a file.<br/>
	 * <br/>
	 * <strong>WARNING</strong>: This method opens a PrintStream to a file. It's
	 * up to the user to close the stream when he no longer needs it (by calling
	 * {@link #getLogStream()} and calling close() on it).
	 *
	 * @param fileLog
	 *            The file to use as a log.
	 * @throws FileNotFoundException
	 */
	public static void setLogStream(File fileLog) throws FileNotFoundException {
		PrintStream fileStream = new PrintStream(fileLog);
		setLogStream(fileStream);
	}

	/**
	 * Returns the output stream used for the log. Default is System.out.
	 *
	 * @return The output stream used for the log. Default is System.out.
	 */
	public static PrintStream getLogStream() {
		return logStream;
	}

	/**
	 * Sets whether to print to the log.
	 *
	 * @param logEnabled
	 *            If true, text will be output to the log.
	 */
	public static void setLogEnabled(boolean logEnabled) {
		GeneralSettings.logEnabled = logEnabled;
	}

	/**
	 * If true, text will be output to the log. Default is false;
	 *
	 * @return If true, text will be output to the log. Default is false;
	 */
	public static boolean isLogEnabled() {
		return (logEnabled && logStream != null);
	}

	/**
	 * Sets the verbosity level of the log.
	 *
	 * @param logVerbosity
	 *            The verbosity level of the log.
	 */
	public static void setLogVerbosity(Log.Verbosity logVerbosity) {
		GeneralSettings.logVerbosity = logVerbosity;
	}

	/**
	 * The verbosity level of the log. Default is VERBOSE.
	 *
	 * @return The verbosity level of the log. Default is VERBOSE.
	 */
	public static Log.Verbosity getLogVerbosity() {
		return logVerbosity;
	}

	/**
	 * Sets the locale of the API.
	 *
	 * @param APILocale
	 *            The locale of the API.
	 */
	public static void setAPILocale(Locale APILocale) {
		GeneralSettings.APILocale = APILocale;
	}

	/**
	 * The locale of the API. Default is en-US.
	 *
	 * @return The locale of the API. Default is en-US.
	 */
	public static Locale getAPILocale() {
		return APILocale;
	}

	/**
	 * The locale string of the API. Default is "en-US".
	 *
	 * @return The locale string of the API. Default is "en-US".
	 */
	public static String getAPILocaleFormatted() {
		StringBuffer formatted = new StringBuffer();
		if (APILocale.getLanguage() != null
				&& !APILocale.getLanguage().equals("")) {
			formatted.append(APILocale.getLanguage());
		}
		if (APILocale.getCountry() != null
				&& !APILocale.getCountry().equals("")) {
			formatted.append("-").append(APILocale.getCountry());
		}
		return formatted.toString();
	}

	/**
	 * This method will store the state of the GeneralSettings to the provided
	 * state object.
	 *
	 * @param state
	 *            The state object to use in order to store the GeneralSettings
	 *            state.
	 */
	public static void storeState(GeneralSettingsState state) {
		state.storeAPIKey(getApiKey());
		state.storeAPILocale(getAPILocale());
		state.storeLogVerbosity(getLogVerbosity());
		state.storeLogEnabled(isLogEnabled());
		state.storeLogStream(getLogStream());
	}

	/**
	 * This method will restore the state of the GeneralSettings from the
	 * provided state object.
	 *
	 * @param state
	 *            The state object to use in order to restore the
	 *            GeneralSettings state.
	 */
	public static void restoreState(GeneralSettingsState state) {
		setApiKey(state.restoreAPIKey());
		setAPILocale(state.restoreAPILocale());
		setLogVerbosity(state.restoreLogVerbosity());
		setLogEnabled(state.restoreLogEnabled());
		setLogStream(state.restoreLogStream());
	}

	/**
	 * This class provides useful utilities.
	 *
	 * @author Savvas Dalkitsis
	 */
	public static class Utilities {

		private Utilities() {

		}

		/**
		 * This method will open a connection to the provided url and return its
		 * response.
		 *
		 * @param url
		 *            The url to open a connection to.
		 * @return The respone.
		 * @throws IOException
		 */
		public static String readUrlResponse(URL url) throws IOException {
			URLConnection yc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc
					.getInputStream()));
			String inputLine;
			StringBuffer responce = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				responce.append(inputLine);
			}
			in.close();
			return responce.toString();
		}

		/**
		 * This method posts data to a URL and returns the response.
		 *
		 * @param url
		 *            The url to post the data to.
		 * @param post
		 *            The data to post. The data passed will be handled as
		 *            string pairs. If the data supplied is of odd amount, the
		 *            last one will be skipped.
		 * @return The response.
		 * @throws IOException
		 */
		public static String postToUrl(URL url, String... post)
				throws IOException {
			URLConnection conn = url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			int last = post.length - 1;
			if (post.length % 2 != 0) {
				last--;
			}
			if (post.length == 1) {
				last = -1;
			}
			StringBuffer data = new StringBuffer();
			for (int i = 0; i <= last; i = i + 2) {
				data.append(URLEncoder.encode(post[i], "UTF-8")).append("=")
						.append(URLEncoder.encode(post[i + 1], "UTF-8"))
						.append("&");
			}
			if (data.length() > 0) {
				data.deleteCharAt(data.length() - 1);
			}

			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(data.toString());
			out.flush();
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		}
	}

}
